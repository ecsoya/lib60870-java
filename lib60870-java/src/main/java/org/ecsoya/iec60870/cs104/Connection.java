/*******************************************************************************
 * Copyright (C) 2019 Ecsoya (jin.liu@soyatec.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.ecsoya.iec60870.cs104;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;

import javax.net.SocketFactory;

import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ASDUParsingException;
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
import org.ecsoya.iec60870.core.ConnectionException;
import org.ecsoya.iec60870.core.Master;
import org.ecsoya.iec60870.core.file.FileClient;
import org.ecsoya.iec60870.core.file.IFileReceiver;

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

		@Override
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

	private int unconfirmedReceivedIMessages; // number of unconfirmed messages received
	/* T2 timeout handling */
	private long lastConfirmationTime; // timestamp when the last confirmation message was sent

	private boolean timeoutT2Triggered = false;
	private Socket socket = null;

	private DataOutputStream netStream = null;

	private boolean autostart = true;

	private FileClient fileClient = null;
	private String hostname;

	protected int tcpPort;

	private int connectionID;

	private APCIParameters apciParameters;

	private ConnectionStatistics statistics = new ConnectionStatistics();

	private int connectTimeoutInMs = 5000;

	private ConnectionHandler connectionHandler = null;

	private Object connectionHandlerParameter = null;

	public Connection(String hostname) {
		this(hostname, 2404);
	}

	public Connection(String hostname, APCIParameters apciParameters, ApplicationLayerParameters alParameters) {
		this(hostname, 2404, apciParameters, alParameters);
	}

	public Connection(String hostname, int tcpPort) {
		this(hostname, 2404, new APCIParameters(), new ApplicationLayerParameters());
	}

	public Connection(String hostname, int tcpPort, APCIParameters apciParameters,
			ApplicationLayerParameters alParameters) {
		super(alParameters);
		this.hostname = hostname;
		this.apciParameters = apciParameters;
		this.tcpPort = tcpPort;
		this.connectTimeoutInMs = apciParameters.getT0() * 1000;

		connectionCounter++;
		connectionID = connectionCounter;
	}

	public int getConnectionID() {
		return connectionID;
	}

	private boolean checkConfirmTimeout(long currentTime) {
		if ((currentTime - lastConfirmationTime) >= (apciParameters.getT2() * 1000)) {
			return true;
		} else {
			return false;
		}
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

			if (checkSequenceNumber(frameRecvSequenceNumber) == false) {
				return false;
			}

			receiveSequenceNumber = (receiveSequenceNumber + 1) % 32768;
			unconfirmedReceivedIMessages++;

			try {
				ASDU asdu = new ASDU(getApplicationLayerParameters(), buffer, 6, msgSize);

				boolean messageHandled = false;

				if (fileClient != null) {
					messageHandled = fileClient.handleFileAsdu(asdu);
				}

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

			if (checkSequenceNumber(seqNo) == false) {
				return false;
			}
		} else if ((buffer[2] & 0x03) == 0x03) { /* U format frame */

			uMessageTimeout = 0;

			if (buffer[2] == 0x43) { // Check for TESTFR_ACT message
				statistics.increaseRcvdTestFrActCounter();
				debugLog("RCVD TESTFR_ACT");
				debugLog("SEND TESTFR_CON");

				writeMessage(TESTFR_CON_MSG, 0, TESTFR_CON_MSG.length);

			} else if (buffer[2] == 0x83) { /* TESTFR_CON */
				debugLog("RCVD TESTFR_CON");
				statistics.increaseRcvdTestFrConCounter();
				outStandingTestFRConMessages = 0;
			} else if (buffer[2] == 0x07) { /* STARTDT ACT */
				debugLog("RCVD STARTDT_ACT");

				writeMessage(STARTDT_CON_MSG, 0, STARTDT_CON_MSG.length);
			} else if (buffer[2] == 0x0b) { /* STARTDT_CON */
				debugLog("RCVD STARTDT_CON");

				handleConnectionChanged(ConnectionEvent.STARTDT_CON_RECEIVED);

			} else if (buffer[2] == 0x23) { /* STOPDT_CON */
				debugLog("RCVD STOPDT_CON");

				handleConnectionChanged(ConnectionEvent.STOPDT_CON_RECEIVED);
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
					if (seqNo == sendSequenceNumber) {
						seqNoIsValid = true;
					}
				} else {
					// Two cases are required to reflect sequence number overflow
					if (sentASDUs[oldestSentASDU].seqNo <= sentASDUs[newestSentASDU].seqNo) {
						if ((seqNo >= sentASDUs[oldestSentASDU].seqNo) && (seqNo <= sentASDUs[newestSentASDU].seqNo)) {
							seqNoIsValid = true;
						}

					} else {
						if ((seqNo >= sentASDUs[oldestSentASDU].seqNo) || (seqNo <= sentASDUs[newestSentASDU].seqNo)) {
							seqNoIsValid = true;
						}

						counterOverflowDetected = true;
					}

					int latestValidSeqNo = (sentASDUs[oldestSentASDU].seqNo - 1) % 32768;

					if (latestValidSeqNo == seqNo) {
						seqNoIsValid = true;
					}
				}

				if (seqNoIsValid == false) {
					debugLog("Received sequence number out of range");
					return false;
				}

				if (oldestSentASDU != -1) {
					do {
						if (counterOverflowDetected == false) {
							if (seqNo < sentASDUs[oldestSentASDU].seqNo) {
								break;
							}
						} else {
							if (seqNo == ((sentASDUs[oldestSentASDU].seqNo - 1) % 32768)) {
								break;
							}
						}

						oldestSentASDU = (oldestSentASDU + 1) % maxSentASDUs;

						int checkIndex = (newestSentASDU + 1) % maxSentASDUs;

						if (oldestSentASDU == checkIndex) {
							oldestSentASDU = -1;
							break;
						}

						if (sentASDUs[oldestSentASDU].seqNo == seqNo) {
							break;
						}

					} while (true);
				}
			}
		}

		return true;
	}

	@Override
	protected void beforeConnection() {
		super.beforeConnection();
		resetConnection();

		resetT3Timeout();
	}

	private void connectSocketWithTimeout() throws SocketException {
		InetAddress ipAddress;
		SocketAddress remoteEP;

		try {
			ipAddress = InetAddress.getByName(hostname);
			remoteEP = new InetSocketAddress(ipAddress, tcpPort);
		} catch (UnknownHostException e) {
			throw new SocketException("87"); // wrong argument
		}

		try {
			// Create a TCP/IP socket.
			socket = SocketFactory.getDefault().createSocket();
			socket.setSoTimeout(connectTimeoutInMs);
			socket.setTcpNoDelay(true);
			socket.connect(remoteEP, connectTimeoutInMs);

		} catch (Exception e) {
			socket = null;
			debugLog("ObjectDisposedException -> Connect canceled");

			throw new SocketException("10060"); // WSA_OPERATION_ABORTED
		}

		try {
			netStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			throw new SocketException("10070"); // WSA_OPERATION_ABORTED
		}
	}

	@Override
	protected void debugLog(String message) {
		super.debugLog("CS104 MASTER: " + message);
	}

	public void getDirectory(int commonAddress) throws ConnectionException {
		ASDU getDirectoryAsdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.REQUEST, false, false,
				(byte) 0, commonAddress, false);

		InformationObject io = new FileCallOrSelect(0, NameOfFile.DEFAULT, (byte) 0, SelectAndCallQualifier.DEFAULT);

		getDirectoryAsdu.addInformationObject(io);

		sendASDU(getDirectoryAsdu);
	}

	@Override
	public void getFile(int commonAddress, int informationObjectAddress, NameOfFile nameOfFile, IFileReceiver receiver)
			throws ConnectionException {
		if (fileClient == null) {
			fileClient = new FileClient(this, (msg) -> debugLog(msg));
		}

		fileClient.requestFile(commonAddress, informationObjectAddress, nameOfFile, receiver);
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

	@Override
	protected boolean startConnection() throws ConnectionException {
		try {
			// Connect to a remote device.
			connectSocketWithTimeout();

			debugLog("Socket connected to " + socket.getRemoteSocketAddress().toString());

			handleConnectionChanged(ConnectionEvent.OPENED);

			return true;

		} catch (SocketException se) {
			debugLog("SocketException: " + se.toString());

			handleConnectionChanged(ConnectionEvent.CONNECT_FAILED);
			throw new ConnectionException(se);
		}

	}

	@Override
	public void run() throws IOException {
		byte[] bytes = new byte[300];
		IOException exception = null;
		try {
			// Receive a message from from the remote device.
			int bytesRec = receiveMessage(bytes);

			if (bytesRec > 0) {

//                DebugLog("RCVD: " + BitConverter.ToString(bytes, 0, bytesRec));
				debugLog("RCVD: " + Arrays.toString(ByteBuffer.wrap(bytes, 0, bytesRec).array()));

				statistics.increaseRcvdMsgCounter();

				boolean handleMessage = handleReceivedMessage(bytes, bytesRec);

				if (handleMessage) {
					if (checkMessage(bytes, bytesRec) == false) {
						/* close connection on error */
						exception = new IOException("Invalid message");
					}
				}

				if (unconfirmedReceivedIMessages >= apciParameters.getW()) {
					lastConfirmationTime = System.currentTimeMillis();

					unconfirmedReceivedIMessages = 0;
					timeoutT2Triggered = false;

					sendSMessage();
				}

			} else if (bytesRec == -1) {
				exception = new IOException("no message");
			}

			if (handleTimeouts() == false) {
				exception = new IOException("timeout");
			}

			if (useSendMessageQueue) {
				if (sendNextWaitingASDU() == true) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (IOException e) {
			exception = e;
		} catch (ConnectionException e) {
			exception = new IOException("no connect", e);
		}
		if (exception != null) {
			throw exception;
		}
	}

	protected void closeConnection() {
		// Release the socket.
		try {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();

			netStream.close();

			handleConnectionChanged(ConnectionEvent.CLOSED);
		} catch (Exception e) {

			debugLog("Close socket failed: " + e.getMessage());
		} finally {
			socket = null;
			netStream = null;
		}

	}

	private void handleConnectionChanged(ConnectionEvent event) {
		if (connectionHandler != null) {
			connectionHandler.invoke(connectionHandlerParameter, event);
		}
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
			if (checkConfirmTimeout(currentTime)) {
				lastConfirmationTime = currentTime;

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

				if ((currentTime - sentASDUs[oldestSentASDU].sentTime) >= (apciParameters.getT1() * 1000)) {
					return false;
				}
			}
		}

		return true;
	}

	private void internalSendASDU(ASDU asdu) throws ConnectionException {
		checkConnection();
		synchronized (socket) {

			if (useSendMessageQueue) {
				synchronized (waitingToBeSent) {
					waitingToBeSent.push(asdu);
				}

				sendNextWaitingASDU();
			} else {

				if (isSentBufferFull()) {
					throw new ConnectionException("Flow control congestion. Try again later.");
				}

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

	@Override
	protected void afterConnection() {
		super.afterConnection();

		// auto start
		try {
			if (autostart) {
				writeMessage(STARTDT_ACT_MSG, 0, STARTDT_ACT_MSG.length);
			}
		} catch (ConnectionException e1) {
			e1.printStackTrace();
		}
	}

	private boolean isSentBufferFull() {

		if (oldestSentASDU == -1) {
			return false;
		}

		int newIndex = (newestSentASDU + 1) % maxSentASDUs;

		if (newIndex == oldestSentASDU) {
			return true;
		} else {
			return false;
		}
	}

	/// <summary>
/// Determines whether the transmit (send) buffer is full. If true the next send command will throw a ConnectionException
/// </summary>
/// <returns><c>true</c> if this instance is send buffer full; otherwise, <c>false</c>.</returns>
	public boolean isTransmitBufferFull() {
		if (useSendMessageQueue) {
			return false;
		} else {
			return isSentBufferFull();
		}
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

				if (currentIndex == newestSentASDU) {
					nextIndex = -1;
				}

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
			if (is.read(buffer, 0, 1) != 1) {
				return -1;
			}

			if (buffer[0] != 0x68) {
				debugLog("Missing SOF indicator!");

				return -1;
			}

			// read length byte
			if (is.read(buffer, 1, 1) != 1) {
				return -1;
			}

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

		maxSentASDUs = apciParameters.getK();
		oldestSentASDU = -1;
		newestSentASDU = -1;
		sentASDUs = new SentASDU[maxSentASDUs];

		if (useSendMessageQueue) {
			waitingToBeSent = new LinkedList<ASDU>();
		}

		statistics.reset();
	}

	private void resetT3Timeout() {
		nextT3Timeout = System.currentTimeMillis() + (apciParameters.getT3() * 1000);
	}

	@Override
	public void sendASDU(ASDU asdu) throws ConnectionException {
		internalSendASDU(asdu);
	}

	@Override
	public void sendClockSyncCommand(int commonAddress, CP56Time2a time) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new ClockSynchronizationCommand(0, time));

		internalSendASDU(asdu);
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
	@Override
	public void sendControlCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			InformationObject informationObject) throws ConnectionException {

		ASDU controlCommand = new ASDU(getApplicationLayerParameters(), causeOfTransmission, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		controlCommand.addInformationObject(informationObject);

		internalSendASDU(controlCommand);
	}

	@Override
	public void sendCounterInterrogationCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			byte qualifierOfCounter) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), causeOfTransmission, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new CounterInterrogationCommand(0, qualifierOfCounter));

		internalSendASDU(asdu);
	}

	@Override
	public void sendDelayAcquisitionCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			CP16Time2a delay) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new DelayAcquisitionCommand(0, delay));

		internalSendASDU(asdu);
	}

	private int sendIMessage(ASDU asdu) throws ConnectionException {
		BufferFrame frame = new BufferFrame(new byte[260], 6); /* reserve space for ACPI */
		asdu.encode(frame, getApplicationLayerParameters());

		byte[] buffer = frame.getBuffer();

		int msgSize = frame.getMsgSize(); /* ACPI + ASDU */

		buffer[0] = 0x68;

		/* set size field */
		buffer[1] = (byte) (msgSize - 2);

		buffer[2] = (byte) ((sendSequenceNumber % 128) * 2);
		buffer[3] = (byte) (sendSequenceNumber / 128);

		buffer[4] = (byte) ((receiveSequenceNumber % 128) * 2);
		buffer[5] = (byte) (receiveSequenceNumber / 128);

		writeMessage(buffer, 0, msgSize);

		sendSequenceNumber = (sendSequenceNumber + 1) % 32768;

		unconfirmedReceivedIMessages = 0;
		timeoutT2Triggered = false;

		return sendSequenceNumber;

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

	@Override
	public void sendInterrogationCommand(CauseOfTransmission cot, int commonAddress, byte qualifierOfInterrogation)
			throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), cot, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new InterrogationCommand(0, qualifierOfInterrogation));

		internalSendASDU(asdu);
	}

	private void writeMessage(byte[] buf, int offset, int length) throws ConnectionException {
		checkConnection();
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

		checkConnection();

		try {

			synchronized (waitingToBeSent) {

				while (waitingToBeSent.size() > 0) {

					if (isSentBufferFull() == true) {
						break;
					}

					ASDU asdu = waitingToBeSent.pop();

					if (asdu != null) {
						sendIMessageAndUpdateSentASDUs(asdu);
						sentAsdu = true;
					} else {
						break;
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ConnectionException("connection lost");
		}

		return sentAsdu;
	}

	@Override
	public void sendReadCommand(int commonAddress, int informationObjectAddress) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.REQUEST, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new ReadCommand(informationObjectAddress));

		internalSendASDU(asdu);
	}

	@Override
	public void sendResetProcessCommand(CauseOfTransmission causeOfTransmission, int commonAddress, byte qualifier)
			throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new ResetProcessCommand(0, qualifier));

		internalSendASDU(asdu);
	}

	private void sendSMessage() throws ConnectionException {
		byte[] msg = new byte[6];

		msg[0] = 0x68;
		msg[1] = 0x04;
		msg[2] = 0x01;
		msg[3] = 0;
		msg[4] = (byte) ((receiveSequenceNumber % 128) * 2);
		msg[5] = (byte) (receiveSequenceNumber / 128);

		writeMessage(msg, 0, msg.length);

	}

	public void sendStartDT() throws ConnectionException {
		writeMessage(STARTDT_ACT_MSG, 0, STARTDT_ACT_MSG.length);
	}

	protected void sendStartDT_CON() throws ConnectionException {
		writeMessage(STARTDT_CON_MSG, 0, STARTDT_CON_MSG.length);
	}

	/// <summary>
/// Stop data transmission on this connection
/// </summary>
	public void sendStopDT() throws ConnectionException {
		writeMessage(STOPDT_ACT_MSG, 0, STOPDT_ACT_MSG.length);
	}

	protected void sendStopDT_CON() throws ConnectionException {
		writeMessage(STOPDT_CON_MSG, 0, STOPDT_CON_MSG.length);
	}

	@Override
	public void sendTestCommand(int commonAddress) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new TestCommand());

		internalSendASDU(asdu);
	}

	@Override
	public void sendTestCommandWithCP56Time2a(int commonAddress, short testSequenceNumber, CP56Time2a timestamp)
			throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new TestCommandWithCP56Time2a(testSequenceNumber, timestamp));

		internalSendASDU(asdu);
	}

	protected void sendTestFR_ACT() throws ConnectionException {
		writeMessage(TESTFR_ACT_MSG, 0, TESTFR_ACT_MSG.length);
	}

	protected void sendTestFR_CON() throws ConnectionException {
		writeMessage(TESTFR_CON_MSG, 0, TESTFR_CON_MSG.length);
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

	public void setUseSendMessageQueue(boolean useSendMessageQueue) {
		this.useSendMessageQueue = useSendMessageQueue;
	}
}
