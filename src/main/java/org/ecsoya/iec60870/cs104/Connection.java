//====================================================================================================
//The Free Edition of C# to Java Converter limits conversion output to 100 lines per file.

//To subscribe to the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================

package org.ecsoya.iec60870.cs104;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.ConnectionException;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.ie.ClockSynchronizationCommand;
import org.ecsoya.iec60870.asdu.ie.CounterInterrogationCommand;
import org.ecsoya.iec60870.asdu.ie.DelayAcquisitionCommand;
import org.ecsoya.iec60870.asdu.ie.FileCallOrSelect;
import org.ecsoya.iec60870.asdu.ie.InterrogationCommand;
import org.ecsoya.iec60870.asdu.ie.ReadCommand;
import org.ecsoya.iec60870.asdu.ie.ResetProcessCommand;
import org.ecsoya.iec60870.asdu.ie.TestCommand;
import org.ecsoya.iec60870.asdu.ie.TestCommandWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;
import org.ecsoya.iec60870.asdu.ie.value.SelectAndCallQualifier;
import org.ecsoya.iec60870.conn.FileClient;
import org.ecsoya.iec60870.conn.IFileReceiver;
import org.ecsoya.iec60870.conn.Master;

/**
 * A single connection to a CS 104 (IEC 60870-5-104) server. Implements the \ref
 * Master interface.
 */
public class Connection extends Master {
	/**********************************************/
	/* data structure for k-size sent ASDU buffer */
	private final static class SentASDU {
		public long sentTime; // required for T1 timeout
		public int seqNo;

		public SentASDU clone() {
			SentASDU varCopy = new SentASDU();

			varCopy.sentTime = this.sentTime;
			varCopy.seqNo = this.seqNo;

			return varCopy;
		}
	}

	private static byte[] STARTDT_ACT_MSG = new byte[] { 0x68, 0x04, 0x07, 0x00, 0x00, 0x00 };
	private static byte[] STARTDT_CON_MSG = new byte[] { 0x68, 0x04, 0x0b, 0x00, 0x00, 0x00 };
	private static byte[] STOPDT_ACT_MSG = new byte[] { 0x68, 0x04, 0x13, 0x00, 0x00, 0x00 };
	private static byte[] STOPDT_CON_MSG = new byte[] { 0x68, 0x04, 0x23, 0x00, 0x00, 0x00 };
	private static byte[] TESTFR_ACT_MSG = new byte[] { 0x68, 0x04, 0x43, 0x00, 0x00, 0x00 };

	private static byte[] TESTFR_CON_MSG = new byte[] { 0x68, 0x04, (byte) 0x83, 0x00, 0x00, 0x00 };
	private static int connectionCounter = 0;

	private int sendSequenceNumber;

	private int receiveSequenceNumber;

	private long uMessageTimeout = 0;
	private int maxSentASDUs; // maximum number of ASDU to be sent without confirmation - parameter k
	private int oldestSentASDU = -1; // index of oldest entry in k-buffer
	private int newestSentASDU = -1; // index of newest entry in k-buffer

	private SentASDU[] sentASDUs = null; // the k-buffer

	/**********************************************/

	private boolean checkSequenceNumbers = true;
	private LinkedList<ASDU> waitingToBeSent = null;

	private boolean useSendMessageQueue = true;
	private long nextT3Timeout;

	private int outStandingTestFRConMessages = 0;

	private Thread workerThread = null;

	private int unconfirmedReceivedIMessages; // number of unconfirmed messages received
	/* T2 timeout handling */
	private long lastConfirmationTime; // timestamp when the last confirmation message was sent

	private boolean timeoutT2Triggered = false;
	private Socket socket = null;

	private OutputStream netStream = null;

	private boolean autostart = true;

	private FileClient fileClient = null;
	private String hostname;

	protected int tcpPort;
	private boolean running = false;
	private boolean connecting = false;
	private boolean socketError;

	private SocketException lastException;
	private int connectionID;

	private APCIParameters apciParameters;
	private ApplicationLayerParameters alParameters;

	private ConnectionStatistics statistics = new ConnectionStatistics();

	private int connectTimeoutInMs = 1000;

	private ConnectionHandler connectionHandler = null;

	private Object connectionHandlerParameter = null;

	public Connection(String hostname) {
		this(hostname, 2404);
	}

	public Connection(String hostname, APCIParameters apciParameters, ApplicationLayerParameters alParameters) {
		this(hostname, 2404, apciParameters.Clone(), (ApplicationLayerParameters) alParameters.clone());
	}

	public Connection(String hostname, int tcpPort) {
		this(hostname, 2404, new APCIParameters(), new ApplicationLayerParameters());
	}

	public Connection(String hostname, int tcpPort, APCIParameters apciParameters,
			ApplicationLayerParameters alParameters) {
		setup(hostname, apciParameters.Clone(), alParameters.clone(), tcpPort);
	}

	public void cancel() {
		if (socket != null)
			try {
				socket.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
	}

	private boolean checkConfirmTimeout(long currentTime) {
		if ((currentTime - lastConfirmationTime) >= (apciParameters.getT2() * 1000))
			return true;
		else
			return false;
	}

	private boolean checkMessage(byte[] buffer, int msgSize) throws ConnectionException {
		long currentTime = System.currentTimeMillis();

		if ((buffer[2] & 1) == 0) { /* I format frame */

			if (timeoutT2Triggered == false) {
				timeoutT2Triggered = true;
				lastConfirmationTime = currentTime; /* start timeout T2 */
			}

			if (msgSize < 7) {
				debugLog("I msg too small!");
				return false;
			}

			int frameSendSequenceNumber = ((buffer[3] * 0x100) + (buffer[2] & 0xfe)) / 2;
			int frameRecvSequenceNumber = ((buffer[5] * 0x100) + (buffer[4] & 0xfe)) / 2;

			debugLog("Received I frame: N(S) = " + frameSendSequenceNumber + " N(R) = " + frameRecvSequenceNumber);

			/*
			 * check the receive sequence number N(R) - connection will be closed on an
			 * unexpected value
			 */
			if (frameSendSequenceNumber != receiveSequenceNumber) {
				debugLog("Sequence error: Close connection!");
				return false;
			}

			if (checkSequenceNumber(frameRecvSequenceNumber) == false)
				return false;

			receiveSequenceNumber = (receiveSequenceNumber + 1) % 32768;
			unconfirmedReceivedIMessages++;

			try {
				ASDU asdu = new ASDU(alParameters, buffer, 6, msgSize);

				boolean messageHandled = false;

				if (fileClient != null)
					messageHandled = fileClient.handleFileAsdu(asdu);

				if (messageHandled == false) {
					handleReceivedASDU(0, asdu);

				}
			} catch (ASDUParsingException e) {
				debugLog("ASDU parsing failed: " + e.getMessage());
				return false;
			}

		} else if ((buffer[2] & 0x03) == 0x01) { /* S format frame */
			int seqNo = (buffer[4] + buffer[5] * 0x100) / 2;

			debugLog("Recv S(" + seqNo + ") (own sendcounter = " + sendSequenceNumber + ")");

			if (checkSequenceNumber(seqNo) == false)
				return false;
		} else if ((buffer[2] & 0x03) == 0x03) { /* U format frame */

			uMessageTimeout = 0;

			if (buffer[2] == 0x43) { // Check for TESTFR_ACT message
				statistics.increaseRcvdTestFrActCounter();
				debugLog("RCVD TESTFR_ACT");
				debugLog("SEND TESTFR_CON");

				sendMessage(TESTFR_CON_MSG, 0, TESTFR_CON_MSG.length);

			} else if (buffer[2] == 0x83) { /* TESTFR_CON */
				debugLog("RCVD TESTFR_CON");
				statistics.increaseRcvdTestFrConCounter();
				outStandingTestFRConMessages = 0;
			} else if (buffer[2] == 0x07) { /* STARTDT ACT */
				debugLog("RCVD STARTDT_ACT");

				sendMessage(STARTDT_CON_MSG, 0, STARTDT_CON_MSG.length);
			} else if (buffer[2] == 0x0b) { /* STARTDT_CON */
				debugLog("RCVD STARTDT_CON");

				if (connectionHandler != null)
					connectionHandler.invoke(connectionHandlerParameter, ConnectionEvent.STARTDT_CON_RECEIVED);

			} else if (buffer[2] == 0x23) { /* STOPDT_CON */
				debugLog("RCVD STOPDT_CON");

				if (connectionHandler != null)
					connectionHandler.invoke(connectionHandlerParameter, ConnectionEvent.STOPDT_CON_RECEIVED);
			}

		} else {
			debugLog("Unknown message type");
			return false;
		}

		resetT3Timeout();

		return true;
	}

	private boolean checkSequenceNumber(int seqNo) {

		if (checkSequenceNumbers) {

			synchronized (sentASDUs) {

				/* check if received sequence number is valid */

				boolean seqNoIsValid = false;
				boolean counterOverflowDetected = false;

				if (oldestSentASDU == -1) { /* if k-Buffer is empty */
					if (seqNo == sendSequenceNumber)
						seqNoIsValid = true;
				} else {
					// Two cases are required to reflect sequence number overflow
					if (sentASDUs[oldestSentASDU].seqNo <= sentASDUs[newestSentASDU].seqNo) {
						if ((seqNo >= sentASDUs[oldestSentASDU].seqNo) && (seqNo <= sentASDUs[newestSentASDU].seqNo))
							seqNoIsValid = true;

					} else {
						if ((seqNo >= sentASDUs[oldestSentASDU].seqNo) || (seqNo <= sentASDUs[newestSentASDU].seqNo))
							seqNoIsValid = true;

						counterOverflowDetected = true;
					}

					int latestValidSeqNo = (sentASDUs[oldestSentASDU].seqNo - 1) % 32768;

					if (latestValidSeqNo == seqNo)
						seqNoIsValid = true;
				}

				if (seqNoIsValid == false) {
					debugLog("Received sequence number out of range");
					return false;
				}

				if (oldestSentASDU != -1) {
					do {
						if (counterOverflowDetected == false) {
							if (seqNo < sentASDUs[oldestSentASDU].seqNo)
								break;
						} else {
							if (seqNo == ((sentASDUs[oldestSentASDU].seqNo - 1) % 32768))
								break;
						}

						oldestSentASDU = (oldestSentASDU + 1) % maxSentASDUs;

						int checkIndex = (newestSentASDU + 1) % maxSentASDUs;

						if (oldestSentASDU == checkIndex) {
							oldestSentASDU = -1;
							break;
						}

						if (sentASDUs[oldestSentASDU].seqNo == seqNo)
							break;

					} while (true);
				}
			}
		}

		return true;
	}

	public void close() {
		if (running) {
			running = false;
			try {
				workerThread.join();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
	}

	/// <summary>
/// Connect this instance.
/// </summary>
/// 
/// The function will throw a SocketException if the connection attempt is rejected or timed out.
/// <exception cref="ConnectionException">description</exception>
	public void connect() throws ConnectionException {

		connectAsync();

		while ((running == false) && (socketError == false)) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

		if (socketError)
			throw new ConnectionException(lastException.getMessage(), lastException);
	}

	/// <summary>
/// Connects to the server (outstation). This is a non-blocking call. Before using the connection
/// you have to check if the connection is already connected and running.
/// </summary>
/// <exception cref="ConnectionException">description</exception>
	public void connectAsync() throws ConnectionException {
		if ((running == false) && (connecting == false)) {
			resetConnection();

			resetT3Timeout();

			workerThread = new Thread(() -> handleConnection());

			workerThread.start();
		} else {
			if (running)
				throw new ConnectionException("already connected",
						new SocketException("10056")); /* WSAEISCONN - Socket is already connected */
			else
				throw new ConnectionException("already connecting",
						new SocketException("10037")); /* WSAEALREADY - Operation already in progress */

		}
	}

	private void connectSocketWithTimeout() throws SocketException {
		InetAddress ipAddress;
		SocketAddress remoteEP;

		try {
			ipAddress = InetAddress.getByName(hostname);
			remoteEP = new InetSocketAddress(ipAddress, tcpPort);
		} catch (Exception e) {
			throw new SocketException("87"); // wrong argument
		}

		// Create a TCP/IP socket.
		socket = new Socket();

		try {
			socket.connect(remoteEP, connectTimeoutInMs);
			socket.setTcpNoDelay(true);
			netStream = socket.getOutputStream();
		} catch (Exception e) {
			socket = null;
			debugLog("ObjectDisposedException -> Connect canceled");

			throw new SocketException("10060"); // WSA_OPERATION_ABORTED
		}
	}

	private void debugLog(String message) {
		if (debug)
			System.out.println("CS104 MASTER CONNECTION " + connectionID + ": " + message);
	}

	public ApplicationLayerParameters getApplicationLayerParameters() {
		return alParameters;
	}

	public void getDirectory(int commonAddress) throws ConnectionException {
		ASDU getDirectoryAsdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.REQUEST, false, false,
				(byte) 0, commonAddress, false);

		InformationObject io = new FileCallOrSelect(0, NameOfFile.DEFAULT, (byte) 0, SelectAndCallQualifier.DEFAULT);

		getDirectoryAsdu.addInformationObject(io);

		sendASDU(getDirectoryAsdu);
	}

	public void getFile(int commonAddress, int informationObjectAddress, NameOfFile nameOfFile, IFileReceiver receiver)
			throws ConnectionException {
		if (fileClient == null)
			fileClient = new FileClient(this, (msg) -> debugLog(msg));

		fileClient.requestFile(commonAddress, informationObjectAddress, nameOfFile, receiver);
	}

	public ApplicationLayerParameters getParameters() {
		return this.alParameters;
	}

	public int getReceiveSequenceNumber() {
		return receiveSequenceNumber;
	}

	public int getSendSequenceNumber() {
		return sendSequenceNumber;
	}

	public ConnectionStatistics getStatistics() {
		return this.statistics;
	}

	private void handleConnection() {

		byte[] bytes = new byte[300];

		try {

			try {

				connecting = true;

				try {
					// Connect to a remote device.
					connectSocketWithTimeout();

					debugLog("Socket connected to " + socket.getRemoteSocketAddress().toString());

					if (autostart) {
						netStream.write(STARTDT_ACT_MSG, 0, STARTDT_ACT_MSG.length);

						statistics.increaseSentMsgCounter();
					}

					running = true;
					socketError = false;
					connecting = false;

					if (connectionHandler != null)
						connectionHandler.invoke(connectionHandlerParameter, ConnectionEvent.OPENED);

				} catch (SocketException se) {
					debugLog("SocketException: " + se.toString());

					running = false;
					socketError = true;
					lastException = se;

					if (connectionHandler != null)
						connectionHandler.invoke(connectionHandlerParameter, ConnectionEvent.CONNECT_FAILED);
				}

				if (running) {

					boolean loopRunning = running;

					while (loopRunning) {

						boolean suspendThread = true;

						try {
							// Receive a message from from the remote device.
							int bytesRec = receiveMessage(bytes);

							if (bytesRec > 0) {

//                            DebugLog("RCVD: " + BitConverter.ToString(bytes, 0, bytesRec));
								debugLog("RCVD: " + Arrays.toString(ByteBuffer.wrap(bytes, 0, bytesRec).array()));

								statistics.increaseRcvdMsgCounter();

								boolean handleMessage = handleReceivedMessage(bytes, bytesRec);

								if (handleMessage) {
									if (checkMessage(bytes, bytesRec) == false) {
										/* close connection on error */
										loopRunning = false;
									}
								}

								if (unconfirmedReceivedIMessages >= apciParameters.getW()) {
									lastConfirmationTime = System.currentTimeMillis();

									unconfirmedReceivedIMessages = 0;
									timeoutT2Triggered = false;

									sendSMessage();
								}

								suspendThread = false;
							} else if (bytesRec == -1)
								loopRunning = false;

							if (handleTimeouts() == false)
								loopRunning = false;

							if (isConnected() == false)
								loopRunning = false;

							if (useSendMessageQueue) {
								if (sendNextWaitingASDU() == true)
									suspendThread = false;
							}

							if (suspendThread)
								Thread.sleep(10);

						} catch (SocketException e) {
							loopRunning = false;
						} catch (IOException e) {
							debugLog("IOException: " + e.getMessage());
							loopRunning = false;
						} catch (ConnectionException e) {
							loopRunning = false;
						}
					}

					debugLog("CLOSE CONNECTION!");

					// Release the socket.
					try {
						socket.shutdownInput();
						socket.shutdownOutput();
					} catch (SocketException e) {
					}

					socket.close();

					netStream.close();

					if (connectionHandler != null)
						connectionHandler.invoke(connectionHandlerParameter, ConnectionEvent.CLOSED);
				}

			} catch (Exception ane) {
				connecting = false;
//				DebugLog("ArgumentNullException: " + ane.ToString());
//			} catch (SocketException se) {
//				DebugLog("SocketException: " + se.ToString());
//			} catch (Exception e) {
//				DebugLog("Unexpected exception: " + e.ToString());
			}

		} catch (Exception e) {
			debugLog(e.getMessage());
		}

		running = false;
		connecting = false;
	}

	private boolean handleTimeouts() throws ConnectionException {
		long currentTime = System.currentTimeMillis();

		if (currentTime > nextT3Timeout) {

			if (outStandingTestFRConMessages > 2) {
				debugLog("Timeout for TESTFR_CON message");

				// close connection
				return false;
			} else {
				try {
					netStream.write(TESTFR_ACT_MSG, 0, TESTFR_ACT_MSG.length);
				} catch (IOException e) {
					throw new ConnectionException(e);
				}

				statistics.increaseSentMsgCounter();
				debugLog("U message T3 timeout");
				uMessageTimeout = currentTime + (apciParameters.getT1() * 1000);
				outStandingTestFRConMessages++;
				resetT3Timeout();
				handleSentMessage(TESTFR_ACT_MSG, TESTFR_ACT_MSG.length);
			}
		}

		if (unconfirmedReceivedIMessages > 0) {
			if (checkConfirmTimeout((long) currentTime)) {
				lastConfirmationTime = (long) currentTime;

				unconfirmedReceivedIMessages = 0;
				timeoutT2Triggered = false;

				sendSMessage(); /* send confirmation message */
			}
		}

		if (uMessageTimeout != 0) {
			if (currentTime > uMessageTimeout) {
				debugLog("U message T1 timeout");
				throw new ConnectionException(new SocketException("10060"));
			}
		}

		/* check if counterpart confirmed I messages */
		synchronized (sentASDUs) {
			if (oldestSentASDU != -1) {

				if (((long) currentTime - sentASDUs[oldestSentASDU].sentTime) >= (apciParameters.getT1() * 1000)) {
					return false;
				}
			}
		}

		return true;
	}

	private void internalSendASDU(ASDU asdu) throws ConnectionException, IOException {
		synchronized (socket) {

			if (running == false)
				throw new ConnectionException("not connected", new SocketException("10057"));

			if (useSendMessageQueue) {
				synchronized (waitingToBeSent) {
					waitingToBeSent.push(asdu);
				}

				sendNextWaitingASDU();
			} else {

				if (isSentBufferFull())
					throw new ConnectionException("Flow control congestion. Try again later.");

				sendIMessageAndUpdateSentASDUs(asdu);
			}
		}
	}

	public boolean isAutostart() {
		return autostart;
	}

	public boolean isCheckSequenceNumbers() {
		return checkSequenceNumbers;
	}

	private boolean isConnected() {
		if (socket == null || !socket.isConnected()) {
			debugLog("Disconnected: ");
			return false;
		}
		if (socket.isClosed()) {
			debugLog("Closed: ");
			return false;
		}
		return true;
	}

	public boolean isRunning() {
		return this.running;
	}

	private boolean isSentBufferFull() {

		if (oldestSentASDU == -1)
			return false;

		int newIndex = (newestSentASDU + 1) % maxSentASDUs;

		if (newIndex == oldestSentASDU)
			return true;
		else
			return false;
	}

	/// <summary>
/// Determines whether the transmit (send) buffer is full. If true the next send command will throw a ConnectionException
/// </summary>
/// <returns><c>true</c> if this instance is send buffer full; otherwise, <c>false</c>.</returns>
	public boolean isTransmitBufferFull() {
		if (useSendMessageQueue)
			return false;
		else
			return isSentBufferFull();
	}

	public boolean isUseSendMessageQueue() {
		return useSendMessageQueue;
	}

	private void printSendBuffer() {

		if (oldestSentASDU != -1) {

			int currentIndex = oldestSentASDU;

			int nextIndex = 0;

			debugLog("------k-buffer------");

			do {
				debugLog(currentIndex + " : S " + sentASDUs[currentIndex].seqNo + " : time "
						+ sentASDUs[currentIndex].sentTime);

				if (currentIndex == newestSentASDU)
					nextIndex = -1;

				currentIndex = (currentIndex + 1) % maxSentASDUs;

			} while (nextIndex != -1);

			debugLog("--------------------");

		}
	}

	private int receiveMessage(byte[] buffer) throws IOException {
		int readLength = 0;

		if (socket != null && socket.isConnected()) {
			InputStream is = socket.getInputStream();
			// wait for first byte
			if (is.read(buffer, 0, 1) != 1)
				return -1;

			if (buffer[0] != 0x68) {
				debugLog("Missing SOF indicator!");

				return -1;
			}

			// read length byte
			if (is.read(buffer, 1, 1) != 1)
				return -1;

			int length = buffer[1];

			// read remaining frame
			if (is.read(buffer, 2, length) != length) {
				debugLog("Failed to read complete frame!");

				return -1;
			}

			readLength = length + 2;
		}

		return readLength;
	}

	private void resetConnection() {
		sendSequenceNumber = 0;
		receiveSequenceNumber = 0;
		unconfirmedReceivedIMessages = 0;
		lastConfirmationTime = Long.MAX_VALUE;
		timeoutT2Triggered = false;
		outStandingTestFRConMessages = 0;

		uMessageTimeout = 0;

		socketError = false;
		lastException = null;

		maxSentASDUs = apciParameters.getK();
		oldestSentASDU = -1;
		newestSentASDU = -1;
		sentASDUs = new SentASDU[maxSentASDUs];

		if (useSendMessageQueue)
			waitingToBeSent = new LinkedList<ASDU>();

		statistics.Reset();
	}

	private void resetT3Timeout() {
		nextT3Timeout = System.currentTimeMillis() + (apciParameters.getT3() * 1000);
	}

	public void sendASDU(ASDU asdu) throws ConnectionException {
		try {
			internalSendASDU(asdu);
		} catch (ConnectionException e) {

			throw e;
		} catch (IOException e) {

			throw new ConnectionException("Write failed: ", e);
		}
	}

	public void sendClockSyncCommand(int commonAddress, CP56Time2a time) throws ConnectionException {
		ASDU asdu = new ASDU(alParameters, CauseOfTransmission.ACTIVATION, false, false, (byte) alParameters.getOA(),
				commonAddress, false);

		asdu.addInformationObject(new ClockSynchronizationCommand(0, time));

		try {
			internalSendASDU(asdu);
		} catch (ConnectionException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

/// The type ID has to match the type of the InformationObject!
/// 
/// C_SC_NA_1 -> SingleCommand
/// C_DC_NA_1 -> DoubleCommand
/// C_RC_NA_1 -> StepCommand
/// C_SC_TA_1 -> SingleCommandWithCP56Time2a
/// C_SE_NA_1 -> SetpointCommandNormalized
/// C_SE_NB_1 -> SetpointCommandScaled
/// C_SE_NC_1 -> SetpointCommandShort
/// C_BO_NA_1 -> Bitstring32Command
/// 
	public void sendControlCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			InformationObject informationObject) throws ConnectionException {

		ASDU controlCommand = new ASDU(alParameters, causeOfTransmission, false, false, (byte) alParameters.getOA(),
				commonAddress, false);

		controlCommand.addInformationObject(informationObject);

		try {
			internalSendASDU(controlCommand);
		} catch (ConnectionException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void sendCounterInterrogationCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			byte qualifierOfCounter) throws ConnectionException {
		ASDU asdu = new ASDU(alParameters, causeOfTransmission, false, false, (byte) alParameters.getOA(),
				commonAddress, false);

		asdu.addInformationObject(new CounterInterrogationCommand(0, qualifierOfCounter));

		try {
			internalSendASDU(asdu);
		} catch (ConnectionException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void sendDelayAcquisitionCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			CP16Time2a delay) throws ConnectionException {
		ASDU asdu = new ASDU(alParameters, CauseOfTransmission.ACTIVATION, false, false, (byte) alParameters.getOA(),
				commonAddress, false);

		asdu.addInformationObject(new DelayAcquisitionCommand(0, delay));

		try {
			internalSendASDU(asdu);
		} catch (ConnectionException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private int sendIMessage(ASDU asdu) throws ConnectionException {
		BufferFrame frame = new BufferFrame(new byte[260], 6); /* reserve space for ACPI */
		asdu.encode(frame, alParameters);

		byte[] buffer = frame.getBuffer();

		int msgSize = frame.getMsgSize(); /* ACPI + ASDU */

		buffer[0] = 0x68;

		/* set size field */
		buffer[1] = (byte) (msgSize - 2);

		buffer[2] = (byte) ((sendSequenceNumber % 128) * 2);
		buffer[3] = (byte) (sendSequenceNumber / 128);

		buffer[4] = (byte) ((receiveSequenceNumber % 128) * 2);
		buffer[5] = (byte) (receiveSequenceNumber / 128);

		if (running) {
			sendMessage(buffer, 0, msgSize);

			sendSequenceNumber = (sendSequenceNumber + 1) % 32768;

			unconfirmedReceivedIMessages = 0;
			timeoutT2Triggered = false;

			return sendSequenceNumber;
		} else {
			if (lastException != null)
				throw new ConnectionException(lastException.getMessage(), lastException);
			else
				throw new ConnectionException("not connected", new SocketException("10057"));
		}

	}

	private void sendIMessageAndUpdateSentASDUs(ASDU asdu) throws ConnectionException {
		synchronized (sentASDUs) {

			int currentIndex = 0;

			if (oldestSentASDU == -1) {
				oldestSentASDU = 0;
				newestSentASDU = 0;

			} else {
				currentIndex = (newestSentASDU + 1) % maxSentASDUs;
			}

			SentASDU sentAsdu = new SentASDU();
			sentAsdu.seqNo = sendIMessage(asdu);
			sentAsdu.sentTime = System.currentTimeMillis();
			sentASDUs[currentIndex] = sentAsdu;
			newestSentASDU = currentIndex;

			printSendBuffer();
		}
	}

	public void sendInterrogationCommand(CauseOfTransmission cot, int commonAddress, byte qualifierOfInterrogation)
			throws ConnectionException {
		ASDU asdu = new ASDU(alParameters, cot, false, false, (byte) alParameters.getOA(), commonAddress, false);

		asdu.addInformationObject(new InterrogationCommand(0, qualifierOfInterrogation));

		try {
			internalSendASDU(asdu);
		} catch (ConnectionException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void sendMessage(byte[] buf, int offset, int length) throws ConnectionException {
		try {
			netStream.write(buf, offset, length);

			statistics.increaseSentMsgCounter();

			handleSentMessage(buf, length);
		} catch (IOException e) {

			throw new ConnectionException("Write failed: ", e);
		}
	}

	private boolean sendNextWaitingASDU() throws ConnectionException {
		boolean sentAsdu = false;

		if (running == false)
			throw new ConnectionException("connection lost");

		try {

			synchronized (waitingToBeSent) {

				while (waitingToBeSent.size() > 0) {

					if (isSentBufferFull() == true)
						break;

					ASDU asdu = waitingToBeSent.pop();

					if (asdu != null) {
						sendIMessageAndUpdateSentASDUs(asdu);
						sentAsdu = true;
					} else
						break;

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			running = false;
			throw new ConnectionException("connection lost");
		}

		return sentAsdu;
	}

	public void sendReadCommand(int commonAddress, int informationObjectAddress) throws ConnectionException {
		ASDU asdu = new ASDU(alParameters, CauseOfTransmission.REQUEST, false, false, (byte) alParameters.getOA(),
				commonAddress, false);

		asdu.addInformationObject(new ReadCommand(informationObjectAddress));

		try {
			internalSendASDU(asdu);
		} catch (ConnectionException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void sendResetProcessCommand(CauseOfTransmission causeOfTransmission, int commonAddress, byte qualifier)
			throws ConnectionException {
		ASDU asdu = new ASDU(alParameters, CauseOfTransmission.ACTIVATION, false, false, (byte) alParameters.getOA(),
				commonAddress, false);

		asdu.addInformationObject(new ResetProcessCommand(0, qualifier));

		try {
			internalSendASDU(asdu);
		} catch (ConnectionException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void sendSMessage() throws ConnectionException {
		byte[] msg = new byte[6];

		msg[0] = 0x68;
		msg[1] = 0x04;
		msg[2] = 0x01;
		msg[3] = 0;
		msg[4] = (byte) ((receiveSequenceNumber % 128) * 2);
		msg[5] = (byte) (receiveSequenceNumber / 128);

		sendMessage(msg, 0, msg.length);

	}

	public void sendStartDT() throws ConnectionException {
		if (running) {
			sendMessage(STARTDT_ACT_MSG, 0, STARTDT_ACT_MSG.length);
		} else {
			if (lastException != null)
				throw new ConnectionException(lastException.getMessage(), lastException);
			else
				throw new ConnectionException("not connected", new SocketException("10057"));
		}
	}

	protected void sendStartDT_CON() throws ConnectionException {
		if (running) {
			sendMessage(STARTDT_CON_MSG, 0, STARTDT_CON_MSG.length);
		} else {
			if (lastException != null)
				throw new ConnectionException(lastException.getMessage(), lastException);
			else
				throw new ConnectionException("not connected", new SocketException("10057"));
		}
	}

	/// <summary>
/// Stop data transmission on this connection
/// </summary>
	public void sendStopDT() throws ConnectionException {
		if (running) {
			sendMessage(STOPDT_ACT_MSG, 0, STOPDT_ACT_MSG.length);
		} else {
			if (lastException != null)
				throw new ConnectionException(lastException.getMessage(), lastException);
			else
				throw new ConnectionException("not connected", new SocketException("10057"));
		}
	}

	protected void sendStopDT_CON() throws ConnectionException {
		if (running) {
			sendMessage(STOPDT_CON_MSG, 0, STOPDT_CON_MSG.length);
		} else {
			if (lastException != null)
				throw new ConnectionException(lastException.getMessage(), lastException);
			else
				throw new ConnectionException("not connected", new SocketException("10057"));
		}
	}

	public void sendTestCommand(int commonAddress) throws ConnectionException {
		ASDU asdu = new ASDU(alParameters, CauseOfTransmission.ACTIVATION, false, false, (byte) alParameters.getOA(),
				commonAddress, false);

		asdu.addInformationObject(new TestCommand());

		try {
			internalSendASDU(asdu);
		} catch (ConnectionException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void sendTestCommandWithCP56Time2a(int commonAddress, short testSequenceNumber, CP56Time2a timestamp)
			throws ConnectionException {
		ASDU asdu = new ASDU(alParameters, CauseOfTransmission.ACTIVATION, false, false, (byte) alParameters.getOA(),
				commonAddress, false);

		asdu.addInformationObject(new TestCommandWithCP56Time2a(testSequenceNumber, timestamp));

		try {
			internalSendASDU(asdu);
		} catch (ConnectionException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	protected void sendTestFR_ACT() throws ConnectionException {
		if (running) {
			sendMessage(TESTFR_ACT_MSG, 0, TESTFR_ACT_MSG.length);
		} else {
			if (lastException != null)
				throw new ConnectionException(lastException.getMessage(), lastException);
			else
				throw new ConnectionException("not connected", new SocketException("10057"));
		}
	}

	protected void sendTestFR_CON() throws ConnectionException {
		if (running) {
			sendMessage(TESTFR_CON_MSG, 0, TESTFR_CON_MSG.length);
		} else {
			if (lastException != null)
				throw new ConnectionException(lastException.getMessage(), lastException);
			else
				throw new ConnectionException("not connected", new SocketException("10057"));
		}
	}

	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}

	public void setCheckSequenceNumbers(boolean checkSequenceNumbers) {
		this.checkSequenceNumbers = checkSequenceNumbers;
	}

	/// <summary>
/// Sets the connection handler. The connection handler is called when
/// the connection is established or closed
/// </summary>
/// <param name="handler">the handler to be called</param>
/// <param name="parameter">user provided parameter that is passed to the handler</param>
	public void setConnectionHandler(ConnectionHandler handler, Object parameter) {
		connectionHandler = handler;
		connectionHandlerParameter = parameter;
	}

	public void setConnectTimeout(int millies) {
		this.connectTimeoutInMs = millies;
	}

	public void setReceiveSequenceNumber(int receiveSequenceNumber) {
		this.receiveSequenceNumber = receiveSequenceNumber;
	}

	public void setSendSequenceNumber(int sendSequenceNumber) {
		this.sendSequenceNumber = sendSequenceNumber;
	}

	private void setup(String hostname, APCIParameters apciParameters, ApplicationLayerParameters alParameters,
			int tcpPort) {
		this.hostname = hostname;
		this.alParameters = alParameters;
		this.apciParameters = apciParameters;
		this.tcpPort = tcpPort;
		this.connectTimeoutInMs = apciParameters.getT0() * 1000;

		connectionCounter++;
		connectionID = connectionCounter;
	}

	public void setUseSendMessageQueue(boolean useSendMessageQueue) {
		this.useSendMessageQueue = useSendMessageQueue;
	}
}
