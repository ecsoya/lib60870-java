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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.ie.ClockSynchronizationCommand;
import org.ecsoya.iec60870.asdu.ie.CounterInterrogationCommand;
import org.ecsoya.iec60870.asdu.ie.DelayAcquisitionCommand;
import org.ecsoya.iec60870.asdu.ie.InterrogationCommand;
import org.ecsoya.iec60870.asdu.ie.ReadCommand;
import org.ecsoya.iec60870.asdu.ie.ResetProcessCommand;
import org.ecsoya.iec60870.core.IMasterCallable;
import org.ecsoya.iec60870.core.file.FileServer;
import org.ecsoya.iec60870.tangible.OutObject;

/**
 * Represents a client (master) connection
 */
public class ClientConnection implements IMasterCallable {
	/* data structure for k-size sent ASDU buffer */
	private final static class SentASDU {
		// required to identify message in server (low-priority) queue
		public long entryTime;
		public int queueIndex; // -1 if ASDU is not from low-priority queue

		public long sentTime; // timestamp when the message was sent (for T1 timeout)
		public int seqNo; // sequence number used to send the message

		@Override
		public SentASDU clone() {
			SentASDU varCopy = new SentASDU();

			varCopy.entryTime = this.entryTime;
			varCopy.queueIndex = this.queueIndex;
			varCopy.sentTime = this.sentTime;
			varCopy.seqNo = this.seqNo;

			return varCopy;
		}
	}

	private static int connectionsCounter = 0;

	private static byte[] STARTDT_CON_MSG = new byte[] { 0x68, 0x04, 0x0b, 0x00, 0x00, 0x00 };

	private static byte[] STOPDT_CON_MSG = new byte[] { 0x68, 0x04, 0x23, 0x00, 0x00, 0x00 };
	private static byte[] TESTFR_CON_MSG = new byte[] { 0x68, 0x04, (byte) 0x83, 0x00, 0x00, 0x00 };
	private static byte[] TESTFR_ACT_MSG = new byte[] { 0x68, 0x04, 0x43, 0x00, 0x00, 0x00 };
	private int connectionID;

	private int sendCount = 0;
	private int receiveCount = 0;

	private int unconfirmedReceivedIMessages = 0; // number of unconfirmed messages received

	/* T3 parameter handling */
	private long nextT3Timeout;
	private int outStandingTestFRConMessages = 0;

	/* T2 parameter handling */
	private boolean timeoutT2Triggered = false;
	private long lastConfirmationTime = Long.MAX_VALUE; // timestamp when the last confirmation message was sent

	private TlsSecurityInformation tlsSecInfo = null;

	private APCIParameters apciParameters;
	private ApplicationLayerParameters alParameters;

	private Server server;

	private ConcurrentLinkedQueue<ASDU> receivedASDUs = null;
	private Thread callbackThread = null;
	private boolean callbackThreadRunning = false;

	private LinkedList<BufferFrame> waitingASDUsHighPrio = null;

	private int maxSentASDUs;

	private int oldestSentASDU = -1;
	private int newestSentASDU = -1;
	private SentASDU[] sentASDUs = null;
	// only available if the server has multiple redundancy groups
	private ASDUQueue asduQueue = null;

	private FileServer fileServer;

	private SocketAddress remoteEndpoint;

	/**
	 * Flag indicating that this connection is the active connection. The active
	 * connection is the only connection that is answering application layer
	 * requests and sends cyclic, and spontaneous messages.
	 */
	private boolean active = false;

	private Socket socket;

	// private NetworkStream socketStream;
	private OutputStream socketStream;

	private boolean running = false;

	private boolean debugOutput = true;

	public ClientConnection(Socket socket, TlsSecurityInformation tlsSecInfo, APCIParameters apciParameters,
			ApplicationLayerParameters parameters, Server server, ASDUQueue asduQueue, boolean debugOutput) {
		connectionsCounter++;
		connectionID = connectionsCounter;

		this.remoteEndpoint = socket.getRemoteSocketAddress();

		this.apciParameters = apciParameters;
		this.alParameters = parameters;
		this.server = server;
		this.asduQueue = asduQueue;
		this.debugOutput = debugOutput;

		resetT3Timeout();

		maxSentASDUs = apciParameters.getK();
		this.sentASDUs = new SentASDU[maxSentASDUs];

		receivedASDUs = new ConcurrentLinkedQueue<>();
		waitingASDUsHighPrio = new LinkedList<BufferFrame>();

		try {
			socketStream = socket.getOutputStream();
		} catch (IOException e) {

			e.printStackTrace();
		}
		this.socket = socket;
		this.tlsSecInfo = tlsSecInfo;

		this.fileServer = new FileServer(this, server.getAvailableFiles(), (String message) -> debugLog(message));

		Thread workerThread = new Thread() {
			@Override
			public void run() {
				handleConnection();
			}
		};

		workerThread.start();
	}

	private boolean rreByteArraysEqual(byte[] array1, byte[] array2) {
		if (array1.length == array2.length) {

			for (int i = 0; i < array1.length; i++) {
				if (array1[i] != array2[i]) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	public void aSDUReadyToSend() {
		if (isActive()) {
			sendWaitingASDUs();
		}
	}

	private boolean checkSequenceNumber(int seqNo) {

		synchronized (sentASDUs) {

			/* check if received sequence number is valid */

			boolean seqNoIsValid = false;
			boolean counterOverflowDetected = false;

			if (oldestSentASDU == -1) { /* if k-Buffer is empty */

				if (seqNo == sendCount) {
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

					/* remove from server (low-priority) queue if required */
					if (sentASDUs[oldestSentASDU].queueIndex != -1) {
						server.markASDUAsConfirmed(sentASDUs[oldestSentASDU].queueIndex,
								sentASDUs[oldestSentASDU].entryTime);
					}

					oldestSentASDU = (oldestSentASDU + 1) % maxSentASDUs;

					int checkIndex = (newestSentASDU + 1) % maxSentASDUs;

					if (oldestSentASDU == checkIndex) {
						oldestSentASDU = -1;
						break;
					}

					if (sentASDUs[oldestSentASDU].seqNo == seqNo) {
						/* we arrived at the seq# that has been confirmed */

						if (oldestSentASDU == newestSentASDU) {
							oldestSentASDU = -1;
						} else {
							oldestSentASDU = (oldestSentASDU + 1) % maxSentASDUs;
						}

						break;
					}

				} while (true);
			}
		}

		return true;
	}

	public void stop() {
		running = false;
	}

	private void debugLog(String msg) {
		if (debugOutput) {
			System.out.print("CS104 SLAVE CONNECTION ");
			System.out.print(connectionID);
			System.out.print(": ");
			System.out.println(msg);
		}
	}

	/**
	 * Gets the connection parameters.
	 *
	 * @return The connection parameters used by the server.
	 */
	@Override
	public final ApplicationLayerParameters getApplicationLayerParameters() {
		return alParameters;
	}

	public final ASDUQueue getASDUQueue() {
		return asduQueue;
	}

	/**
	 * Gets the remote endpoint (client IP address and TCP port)
	 *
	 * <value>The remote IP endpoint</value>
	 */
	public final SocketAddress getRemoteEndpoint() {
		return remoteEndpoint;
	}

	private void handleASDU(ASDU asdu) throws ASDUParsingException {
		debugLog("Handle received ASDU");

		boolean messageHandled = false;

		switch (asdu.getTypeId()) {

		case C_IC_NA_1: /* 100 - interrogation command */

			debugLog("Rcvd interrogation command C_IC_NA_1");

			if ((asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION)
					|| (asdu.getCauseOfTransmission() == CauseOfTransmission.DEACTIVATION)) {
				if (server.interrogationHandler != null) {

					InterrogationCommand irc = (InterrogationCommand) asdu.getElement(0);

					if (server.interrogationHandler.invoke(server.InterrogationHandlerParameter, this, asdu,
							irc.getQOI())) {
						messageHandled = true;
					}
				}
			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDUInternal(asdu);
			}

			break;

		case C_CI_NA_1: /* 101 - counter interrogation command */

			debugLog("Rcvd counter interrogation command C_CI_NA_1");

			if ((asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION)
					|| (asdu.getCauseOfTransmission() == CauseOfTransmission.DEACTIVATION)) {
				if (server.counterInterrogationHandler != null) {

					CounterInterrogationCommand cic = (CounterInterrogationCommand) asdu.getElement(0);

					if (server.counterInterrogationHandler.invoke(server.counterInterrogationHandlerParameter, this,
							asdu, cic.getQualifier())) {
						messageHandled = true;
					}
				}
			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDUInternal(asdu);
			}

			break;

		case C_RD_NA_1: /* 102 - read command */

			debugLog("Rcvd read command C_RD_NA_1");

			if (asdu.getCauseOfTransmission() == CauseOfTransmission.REQUEST) {

				debugLog("Read request for object: " + asdu.getCommonAddress());

				if (server.readHandler != null) {
					ReadCommand rc = (ReadCommand) asdu.getElement(0);

					if (server.readHandler.invoke(server.readHandlerParameter, this, asdu, rc.getObjectAddress())) {
						messageHandled = true;
					}

				}

			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDUInternal(asdu);
			}

			break;

		case C_CS_NA_1: /* 103 - Clock synchronization command */

			debugLog("Rcvd clock sync command C_CS_NA_1");

			if (asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION) {

				if (server.clockSynchronizationHandler != null) {

					ClockSynchronizationCommand csc = (ClockSynchronizationCommand) asdu.getElement(0);

					if (server.clockSynchronizationHandler.invoke(server.clockSynchronizationHandlerParameter, this,
							asdu, csc.getNewTime())) {
						messageHandled = true;
					}
				}

			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDUInternal(asdu);
			}

			break;

		case C_TS_NA_1: /* 104 - test command */

			debugLog("Rcvd test command C_TS_NA_1");

			if (asdu.getCauseOfTransmission() != CauseOfTransmission.ACTIVATION) {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.ACTIVATION_CON);
			}

			this.sendASDUInternal(asdu);

			messageHandled = true;

			break;

		case C_RP_NA_1: /* 105 - Reset process command */

			debugLog("Rcvd reset process command C_RP_NA_1");

			if (asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION) {

				if (server.resetProcessHandler != null) {

					ResetProcessCommand rpc = (ResetProcessCommand) asdu.getElement(0);

					if (server.resetProcessHandler.invoke(server.resetProcessHandlerParameter, this, asdu,
							rpc.getQrp())) {
						messageHandled = true;
					}
				}

			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDUInternal(asdu);
			}

			break;

		case C_CD_NA_1: /* 106 - Delay acquisition command */

			debugLog("Rcvd delay acquisition command C_CD_NA_1");

			if ((asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION)
					|| (asdu.getCauseOfTransmission() == CauseOfTransmission.SPONTANEOUS)) {
				if (server.delayAcquisitionHandler != null) {

					DelayAcquisitionCommand dac = (DelayAcquisitionCommand) asdu.getElement(0);

					if (server.delayAcquisitionHandler.invoke(server.delayAcquisitionHandlerParameter, this, asdu,
							dac.getDelay())) {
						messageHandled = true;
					}
				}
			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDUInternal(asdu);
			}

			break;
		default:
			break;
		}

		if (messageHandled == false) {
			try {
				messageHandled = fileServer.handleFileAsdu(asdu);
			} catch (ASDUParsingException e) {
				messageHandled = false;
				e.printStackTrace();
			}
		}

		if ((messageHandled == false) && (server.asduHandler != null)) {
			if (server.asduHandler.invoke(server.asduHandlerParameter, this, asdu)) {
				messageHandled = true;
			}
		}

		if (messageHandled == false) {
			asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_TYPE_ID);
			this.sendASDUInternal(asdu);
		}

	}

	private void handleConnection() {

		byte[] bytes = new byte[300];

		try {

			try {

				running = true;

//				if (tlsSecInfo != null) {
//
//					DebugLog("Setup TLS");
//
//					SslStream sslStream = new SslStream(socketStream, true, RemoteCertificateValidationCallback);
//
//					boolean authenticationSuccess = false;
//
//					try {
//						sslStream.AuthenticateAsServer(tlsSecInfo.OwnCertificate, true,
//								System.Security.Authentication.SslProtocols.Tls, false);
//
//						if (sslStream.IsAuthenticated == true) {
//							socketStream = sslStream;
//							authenticationSuccess = true;
//						}
//
//					} catch (IOException e) {
//
//						if (e.GetBaseException() != null) {
//							DebugLog("TLS authentication error: " + e.GetBaseException().Message);
//						} else {
//							DebugLog("TLS authentication error: " + e.Message);
//						}
//
//					}
//
//					if (authenticationSuccess == true)
//						socketStream = sslStream;
//					else {
//						DebugLog("TLS authentication failed");
//						running = false;
//					}
//				}

				if (running) {

//					socketStream.ReadTimeout = 50;

					callbackThread = new Thread(() -> processASDUs());
					callbackThread.start();

					resetT3Timeout();
				}

				while (running) {

					try {
						// Receive the response from the remote device.
						int bytesRec = receiveMessage(bytes);

						if (bytesRec > 0) {

//							DebugLog("RCVD: " + BitConverter.ToString(bytes, 0, bytesRec));

							if (handleMessage(bytes, bytesRec) == false) {
								/* close connection on error */
								running = false;
							}

							if (unconfirmedReceivedIMessages >= apciParameters.getW()) {
								lastConfirmationTime = System.currentTimeMillis();
								unconfirmedReceivedIMessages = 0;
								timeoutT2Triggered = false;
								sendSMessage();
							}
						} else if (bytesRec == -1) {
							running = false;
						}
					} catch (IOException e) {
						running = false;
					}

					if (fileServer != null) {
						fileServer.handleFileTransmission();
					}

					if (handleTimeouts() == false) {
						running = false;
					}

					if (running) {
						if (isActive()) {
							sendWaitingASDUs();
						}

						Thread.sleep(1);
					}
				}

				setActive(false);

				debugLog("CLOSE CONNECTION!");

				// Release the socket.

				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();

				socketStream.close();
				socket.close();

				debugLog("CONNECTION CLOSED!");

//			} catch (ArgumentNullException ane) {
//				DebugLog("ArgumentNullException : " + ane.ToString());
			} catch (SocketException se) {
				debugLog("SocketException : " + se.getMessage());
			} catch (Exception e) {
				debugLog("Unexpected exception : " + e.getMessage());
			}

		} catch (Exception e) {
			debugLog(e.getMessage());
		}

		// unmark unconfirmed messages in server queue if k-buffer not empty
		if (oldestSentASDU != -1) {
			server.unmarkAllASDUs();
		}

		server.remove(this);

		if (callbackThreadRunning) {
			callbackThreadRunning = false;
			try {
				callbackThread.join();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

		debugLog("Connection thread finished");
	}

	private boolean handleMessage(byte[] buffer, int msgSize) throws IOException {
		long currentTime = System.currentTimeMillis();

		if ((buffer[2] & 1) == 0) {

			if (msgSize < 7) {
				debugLog("I msg too small!");
				return false;
			}

			if (timeoutT2Triggered == false) {
				timeoutT2Triggered = true;
				lastConfirmationTime = currentTime; /* start timeout T2 */
			}

			int frameSendSequenceNumber = ((buffer[3] * 0x100) + (buffer[2] & 0xfe)) / 2;
			int frameRecvSequenceNumber = ((buffer[5] * 0x100) + (buffer[4] & 0xfe)) / 2;

			debugLog("Received I frame: N(S) = " + frameSendSequenceNumber + " N(R) = " + frameRecvSequenceNumber);

			/*
			 * check the receive sequence number N(R) - connection will be closed on an
			 * unexpected value
			 */
			if (frameSendSequenceNumber != receiveCount) {
				debugLog("Sequence error: Close connection!");
				return false;
			}

			if (checkSequenceNumber(frameRecvSequenceNumber) == false) {
				debugLog("Sequence number check failed");
				return false;
			}

			receiveCount = (receiveCount + 1) % 32768;
			unconfirmedReceivedIMessages++;

			if (isActive()) {

				try {
					ASDU asdu = new ASDU(alParameters, buffer, 6, msgSize);

					// push to handler thread for processing
					debugLog("push received I-message for processing");
					receivedASDUs.add(asdu);
				} catch (ASDUParsingException e) {
					debugLog("ASDU parsing failed: " + e.getMessage());
					return false;
				}
			} else {
				// connection not activated --> skip message
				debugLog("Connection not activated. Skip I message");
			}
		}

		// Check for TESTFR_ACT message
		else if ((buffer[2] & 0x43) == 0x43) {

			debugLog("Send TESTFR_CON");

			socketStream.write(TESTFR_CON_MSG, 0, TESTFR_CON_MSG.length);
		}

		// Check for STARTDT_ACT message
		else if ((buffer[2] & 0x07) == 0x07) {

			debugLog("Send STARTDT_CON");

			if (this.isActive() == false) {
				this.setActive(true);

				this.server.activated(this);
			}

			socketStream.write(STARTDT_CON_MSG, 0, TESTFR_CON_MSG.length);
		}

		// Check for STOPDT_ACT message
		else if ((buffer[2] & 0x13) == 0x13) {

			debugLog("Send STOPDT_CON");

			if (isActive()) {
				this.setActive(false);

				this.server.deactivated(this);
			}

			socketStream.write(STOPDT_CON_MSG, 0, TESTFR_CON_MSG.length);
		}

		// Check for TESTFR_CON message
		else if ((buffer[2] & 0x83) == 0x83) {
			debugLog("Recv TESTFR_CON");

			outStandingTestFRConMessages = 0;
		}

		// S-message
		else if (buffer[2] == 0x01) {

			int seqNo = (buffer[4] + buffer[5] * 0x100) / 2;

			debugLog("Recv S(" + seqNo + ") (own sendcounter = " + sendCount + ")");

			if (checkSequenceNumber(seqNo) == false) {
				return false;
			}

		} else {
			debugLog("Unknown message");
		}

		resetT3Timeout();

		return true;
	}

	private boolean handleTimeouts() {
		long currentTime = System.currentTimeMillis();

		if (currentTime > nextT3Timeout) {

			if (outStandingTestFRConMessages > 2) {
				debugLog("Timeout for TESTFR_CON message");

				// close connection
				return false;
			} else {
				try {
					socketStream.write(TESTFR_ACT_MSG, 0, TESTFR_ACT_MSG.length);

					debugLog("U message T3 timeout");
					outStandingTestFRConMessages++;
					resetT3Timeout();
				} catch (IOException e) {
					running = false;
				}
			}
		}

		if (unconfirmedReceivedIMessages > 0) {

			if ((currentTime - lastConfirmationTime) >= (apciParameters.getT2() * 1000)) {

				lastConfirmationTime = currentTime;
				unconfirmedReceivedIMessages = 0;
				timeoutT2Triggered = false;
				sendSMessage();
			}
		}

		/* check if counterpart confirmed I messages */
		synchronized (sentASDUs) {

			if (oldestSentASDU != -1) {

				if ((currentTime - sentASDUs[oldestSentASDU].sentTime) >= (apciParameters.getT1() * 1000)) {

					printSendBuffer();
					debugLog("I message timeout for " + oldestSentASDU + " seqNo: " + sentASDUs[oldestSentASDU].seqNo);
					return false;
				}
			}
		}

		return true;
	}

	public boolean isActive() {
		return active;
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

	private void printSendBuffer() {

		if (oldestSentASDU != -1) {

			int currentIndex = oldestSentASDU;

			int nextIndex = 0;

			debugLog("------k-buffer------");

			do {
				debugLog(currentIndex + " : S " + sentASDUs[currentIndex].seqNo + " : time "
						+ sentASDUs[currentIndex].sentTime + " : " + sentASDUs[currentIndex].queueIndex);

				if (currentIndex == newestSentASDU) {
					nextIndex = -1;
				} else {
					currentIndex = (currentIndex + 1) % maxSentASDUs;
				}

			} while (nextIndex != -1);

			debugLog("--------------------");

		}

	}

	private void processASDUs() {
		callbackThreadRunning = true;

		while (callbackThreadRunning) {

			while ((receivedASDUs.size() > 0) && (callbackThreadRunning) && (running)) {

				ASDU asdu = receivedASDUs.poll();

//				tangible.OutObject<ASDU> tempOut_asdu = new tangible.OutObject<ASDU>();
//				if (receivedASDUs.TryDequeue(tempOut_asdu)) {
//					asdu = tempOut_asdu.argValue;
				try {
					handleASDU(asdu);
				} catch (ASDUParsingException e) {

					e.printStackTrace();
				}
//				} else {
//					asdu = tempOut_asdu.argValue;
//				}

			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

		debugLog("ProcessASDUs exit thread");
	}

	private int receiveMessage(byte[] buffer) throws IOException {

		int readLength = 0;

//		if (socket.Poll(50, SelectMode.SelectRead)) {
//			// maybe use socketStream.DataAvailable
//
//			// wait for first byte
//			if (socketStream.Read(buffer, 0, 1) != 1)
//				return -1;
//
//			if (buffer[0] != 0x68) {
//				DebugLog("Missing SOF indicator!");
//				return -1;
//			}
//
//			// read length byte
//			if (socketStream.Read(buffer, 1, 1) != 1)
//				return -1;
//
//			int length = buffer[1];
//
//			// read remaining frame
//			if (socketStream.Read(buffer, 2, length) != length) {
//				DebugLog("Failed to read complete frame!");
//				return -1;
//			}
//
//			readLength = length + 2;
//		}
//
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

	private void resetT3Timeout() {
		nextT3Timeout = System.currentTimeMillis() + apciParameters.getT3() * 1000;
	}

	@Override
	public void sendACT_CON(ASDU asdu, boolean negative) {
		asdu.setCauseOfTransmission(CauseOfTransmission.ACTIVATION_CON);
		asdu.setNegative(negative);

		sendASDU(asdu);
	}

	@Override
	public void sendACT_TERM(ASDU asdu) {
		asdu.setCauseOfTransmission(CauseOfTransmission.ACTIVATION_TERMINATION);
		asdu.setNegative(false);

		sendASDU(asdu);
	}

	/// <summary>
/// Send a response ASDU over this connection
/// </summary>
/// <exception cref="ConnectionException">Throws an exception if the connection is no longer active (e.g. because it has been closed by the other side).</exception>
/// <param name="asdu">The ASDU to send</param>
	@Override
	public void sendASDU(ASDU asdu) {
		if (isActive()) {
			sendASDUInternal(asdu);
//		else
//			throw new ConnectionException("Connection not active");
		}
	}

	private void sendASDUInternal(ASDU asdu) {
		if (isActive()) {
			synchronized (waitingASDUsHighPrio) {

				BufferFrame frame = new BufferFrame(new byte[256], 6);

				asdu.encode(frame, alParameters);

				waitingASDUsHighPrio.push(frame);
			}

			sendWaitingASDUs();
		}
	}

	private int sendIMessage(BufferFrame asdu) {

		byte[] buffer = asdu.getBuffer();

		int msgSize = asdu.getMsgSize(); /* ASDU size + ACPI size */

		buffer[0] = 0x68;

		/* set size field */
		buffer[1] = (byte) (msgSize - 2);

		buffer[2] = (byte) ((sendCount % 128) * 2);
		buffer[3] = (byte) (sendCount / 128);

		buffer[4] = (byte) ((receiveCount % 128) * 2);
		buffer[5] = (byte) (receiveCount / 128);

		try {
			synchronized (socketStream) {
				socketStream.write(buffer, 0, msgSize);
//				DebugLog("SEND I (size = " + msgSize + ") : " + BitConverter.ToString(buffer, 0, msgSize));
				sendCount = (sendCount + 1) % 32768;
				unconfirmedReceivedIMessages = 0;
				timeoutT2Triggered = false;
			}
		} catch (IOException e) {
			// socket error --> close connection
			running = false;
		}

		return sendCount;
	}

	private void sendNextAvailableASDU() {
		synchronized (sentASDUs) {
			if (isSentBufferFull()) {
				return;
			}

			OutObject<Long> timestamp = new OutObject<>();
			OutObject<Integer> index = new OutObject<>();

			asduQueue.lockASDUQueue();
			BufferFrame asdu = asduQueue.getNextWaitingASDU(timestamp, index);

			try {

				if (asdu != null) {

					int currentIndex = 0;

					if (oldestSentASDU == -1) {
						oldestSentASDU = 0;
						newestSentASDU = 0;

					} else {
						currentIndex = (newestSentASDU + 1) % maxSentASDUs;
					}

					sentASDUs[currentIndex].entryTime = timestamp.argValue;
					sentASDUs[currentIndex].queueIndex = index.argValue;
					sentASDUs[currentIndex].seqNo = sendIMessage(asdu);
					sentASDUs[currentIndex].sentTime = System.currentTimeMillis();

					newestSentASDU = currentIndex;

					printSendBuffer();
				}
			} finally {
				asduQueue.unlockASDUQueue();
			}
		}
	}

	private boolean sendNextHighPriorityASDU() {
		synchronized (sentASDUs) {
			if (isSentBufferFull()) {
				return false;
			}

			BufferFrame asdu = waitingASDUsHighPrio.pop();

			if (asdu != null) {

				int currentIndex = 0;

				if (oldestSentASDU == -1) {
					oldestSentASDU = 0;
					newestSentASDU = 0;

				} else {
					currentIndex = (newestSentASDU + 1) % maxSentASDUs;
				}

				sentASDUs[currentIndex].queueIndex = -1;
				sentASDUs[currentIndex].seqNo = sendIMessage(asdu);
				sentASDUs[currentIndex].sentTime = System.currentTimeMillis();

				newestSentASDU = currentIndex;

				printSendBuffer();
			} else {
				return false;
			}
		}

		return true;
	}

//public boolean RemoteCertificateValidationCallback (object sender, X509Certificate cert, X509Chain chain, SslPolicyErrors sslPolicyErrors)
//{
//	if (sslPolicyErrors == SslPolicyErrors.None || sslPolicyErrors == SslPolicyErrors.RemoteCertificateChainErrors) {
//
//		if (tlsSecInfo.ChainValidation) {
//
//			X509Chain newChain = new X509Chain ();
//
//			newChain.ChainPolicy.RevocationMode = X509RevocationMode.NoCheck;
//			newChain.ChainPolicy.RevocationFlag = X509RevocationFlag.ExcludeRoot;
//			newChain.ChainPolicy.VerificationFlags = X509VerificationFlags.AllowUnknownCertificateAuthority;
//			newChain.ChainPolicy.VerificationTime = DateTime.Now;
//			newChain.ChainPolicy.UrlRetrievalTimeout = new TimeSpan(0, 0, 0);
//
//			foreach (X509Certificate2 caCert in tlsSecInfo.CaCertificates)
//				newChain.ChainPolicy.ExtraStore.Add(caCert);
//
//			boolean certificateStatus =  newChain.Build(new X509Certificate2(cert.GetRawCertData()));
//
//			if (certificateStatus == false)
//				return false;
//		}
//
//		if (tlsSecInfo.AllowOnlySpecificCertificates) {
//
//			foreach (X509Certificate2 allowedCert in tlsSecInfo.AllowedCertificates) {
//				if (AreByteArraysEqual (allowedCert.GetCertHash (), cert.GetCertHash ())) {
//					return true;
//				}
//			}
//
//			return false;
//		}
//
//		return true;
//	}
//	else
//		return false;
//}

	private void sendSMessage() {
		debugLog("Send S message");

		byte[] msg = new byte[6];

		msg[0] = 0x68;
		msg[1] = 0x04;
		msg[2] = 0x01;
		msg[3] = 0;

		synchronized (socketStream) {
			msg[4] = (byte) ((receiveCount % 128) * 2);
			msg[5] = (byte) (receiveCount / 128);

			try {
				socketStream.write(msg, 0, msg.length);
			} catch (IOException e) {
				// socket error --> close connection
				running = false;
			}
		}
	}

	private void sendWaitingASDUs() {

		synchronized (waitingASDUsHighPrio) {

			while (waitingASDUsHighPrio.size() > 0) {

				if (sendNextHighPriorityASDU() == false) {
					return;
				}

				if (running == false) {
					return;
				}
			}
		}

		// send messages from low-priority queue
		sendNextAvailableASDU();
	}

	public void setActive(boolean value) {
		if (active != value) {

			active = value;

			if (active) {
				debugLog("is active");
			} else {
				debugLog("is not active");
			}
		}
	}

}
