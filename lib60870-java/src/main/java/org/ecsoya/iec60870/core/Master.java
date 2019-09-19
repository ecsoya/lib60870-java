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
package org.ecsoya.iec60870.core;

import java.io.IOException;
import java.net.SocketException;

import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;
import org.ecsoya.iec60870.core.file.IFileReceiver;
import org.ecsoya.iec60870.core.handler.ASDUReceivedHandler;
import org.ecsoya.iec60870.core.handler.RawMessageHandler;

/**
 * Common interface for CS104 and CS101 balanced and unbalanced master
 */
public abstract class Master implements IConnection {

	private boolean debugLog;

	private RawMessageHandler recvRawMessageHandler = null;
	private Object recvRawMessageHandlerParameter = null;

	private RawMessageHandler sentMessageHandler = null;
	private Object sentMessageHandlerParameter = null;

	private ASDUReceivedHandler asduReceivedHandler = null;
	private Object asduReceivedHandlerParameter = null;

	private boolean running = false;
	private boolean connecting = false;
	private ConnectionException connectingException = null;
	private Thread workerThread = null;

	private final ApplicationLayerParameters applicationLayerParameters;

	public Master(ApplicationLayerParameters applicationLayerParameters) {
		if (applicationLayerParameters != null) {
			this.applicationLayerParameters = applicationLayerParameters;
		} else {
			this.applicationLayerParameters = new ApplicationLayerParameters();
		}
	}

	/**
	 * Get the application layer parameters used by this master instance
	 *
	 * @return used application layer parameters
	 */
	public ApplicationLayerParameters getApplicationLayerParameters() {
		return applicationLayerParameters;
	}

	/**
	 * Gets the file.
	 *
	 * @param commonAddress            Ca.
	 * @param informationObjectAddress Ioa.
	 * @param nameOfFile               Nof.
	 * @param receiver                 Receiver.
	 * @throws ConnectionException TODO
	 */
	public abstract void getFile(int commonAddress, int informationObjectAddress, NameOfFile nameOfFile,
			IFileReceiver receiver) throws ConnectionException;

	protected boolean handleReceivedASDU(int slaveAddress, ASDU asdu) {
		if (asduReceivedHandler != null) {
			return asduReceivedHandler.invoke(asduReceivedHandlerParameter, slaveAddress, asdu);
		}
		return false;
	}

	protected boolean handleReceivedMessage(byte[] message, int messageSize) {
		if (recvRawMessageHandler != null) {
			return recvRawMessageHandler.invoke(recvRawMessageHandlerParameter, message, messageSize);
		}
		return false;
	}

	protected boolean handleSentMessage(byte[] message, int messageSize) {
		if (sentMessageHandler != null) {
			return sentMessageHandler.invoke(sentMessageHandlerParameter, message, messageSize);
		}
		return false;
	}

	public boolean isDebugLog() {
		return debugLog;
	}

	public void setDebugLog(boolean debugLog) {
		this.debugLog = debugLog;
	}

	protected void debugLog(String message) {
		if (debugLog) {
			System.out.println(message);
		}
	}

	/**
	 * Sends an arbitrary ASDU to the connected slave
	 *
	 * @param asdu The ASDU to send
	 * @throws ConnectionException TODO
	 */
	public abstract void sendASDU(ASDU asdu) throws ConnectionException;

	/**
	 * Sends a clock synchronization command (C_CS_NA_1 typeID: 103).
	 *
	 * @param commonAddress Common address
	 * @param time          the new time to set
	 * @throws ConnectionException TODO
	 * @exception ConnectionException description
	 */
	public abstract void sendClockSyncCommand(int commonAddress, CP56Time2a time) throws ConnectionException;

	/**
	 * Sends the control command.
	 *
	 *
	 * The type ID has to match the type of the InformationObject!
	 *
	 * C_SC_NA_1 -> SingleCommand C_DC_NA_1 -> DoubleCommand C_RC_NA_1 ->
	 * StepCommand C_SC_TA_1 -> SingleCommandWithCP56Time2a C_SE_NA_1 ->
	 * SetpointCommandNormalized C_SE_NB_1 -> SetpointCommandScaled C_SE_NC_1 ->
	 * SetpointCommandShort C_BO_NA_1 -> Bitstring32Command
	 *
	 * @param causeOfTransmission Cause of transmission (use ACTIVATION to start a
	 *                            control sequence)
	 * @param commonAddress       Common address
	 * @param informationObject   Information object of the command
	 * @throws ConnectionException TODO
	 * @exception ConnectionException description
	 */
	public abstract void sendControlCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			InformationObject informationObject) throws ConnectionException;

	/**
	 * Sends the counter interrogation command (C_CI_NA_1 typeID: 101)
	 *
	 * @param causeOfTransmission Cause of transmission
	 * @param commonAddress       Common address
	 * @param qualifierOfCounter  Qualifier of counter interrogation command
	 * @exception ConnectionException description
	 */
	public abstract void sendCounterInterrogationCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			byte qualifierOfCounter) throws ConnectionException;

	/**
	 * Sends a delay acquisition command (C_CD_NA_1 typeID: 106).
	 *
	 * @param causeOfTransmission Cause of transmission
	 * @param commonAddress       Common address
	 * @param delay               delay for acquisition
	 * @throws ConnectionException TODO
	 * @exception ConnectionException description
	 */
	public abstract void sendDelayAcquisitionCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			CP16Time2a delay) throws ConnectionException;

	/**
	 * Sends the interrogation command.
	 *
	 * @param cot                      Cause of transmission
	 * @param commonAddress            Common address
	 * @param qualifierOfInterrogation Qualifier of interrogation (20 = station
	 *                                 interrogation)
	 * @exception ConnectionException description
	 */
	public abstract void sendInterrogationCommand(CauseOfTransmission cot, int commonAddress,
			byte qualifierOfInterrogation) throws ConnectionException;

	/**
	 * Sends a read command (C_RD_NA_1 typeID: 102).
	 *
	 *
	 * This will send a read command C_RC_NA_1 (102) to the slave/outstation. The
	 * COT is always REQUEST (5). It is used to implement the cyclical polling of
	 * data application function.
	 *
	 * @param commonAddress            Common address
	 * @param informationObjectAddress Information object address
	 * @throws ConnectionException TODO
	 * @exception ConnectionException description
	 */
	public abstract void sendReadCommand(int commonAddress, int informationObjectAddress) throws ConnectionException;

	/**
	 * Sends a reset process command (C_RP_NA_1 typeID: 105).
	 *
	 * @param causeOfTransmission Cause of transmission
	 * @param commonAddress       Common address
	 * @param qualifier           Qualifier of reset process command
	 * @throws ConnectionException TODO
	 * @exception ConnectionException description
	 */
	public abstract void sendResetProcessCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			byte qualifier) throws ConnectionException;

	/**
	 * Sends a test command (C_TS_NA_1 typeID: 104).
	 *
	 *
	 * Not required and supported by IEC 60870-5-104.
	 *
	 * @param commonAddress Common address
	 * @throws ConnectionException TODO
	 * @exception ConnectionException description
	 */
	public abstract void sendTestCommand(int commonAddress) throws ConnectionException;

	/**
	 * Sends a test command with CP56Time2a time (C_TS_TA_1 typeID: 107).
	 *
	 * @param commonAddress      Common address
	 * @param testSequenceNumber test sequence number
	 * @param timestamp          test timestamp
	 * @throws ConnectionException TODO
	 * @exception ConnectionException description
	 */
	public abstract void sendTestCommandWithCP56Time2a(int commonAddress, short testSequenceNumber,
			CP56Time2a timestamp) throws ConnectionException;

	public void setASDUReceivedHandler(ASDUReceivedHandler handler, Object parameter) {
		asduReceivedHandler = handler;
		asduReceivedHandlerParameter = parameter;
	}

	public final void setDebug(boolean value) {
		debugLog = value;
	}

	/**
	 * Sets the raw message handler for receoved messages
	 *
	 * @param handler   Handler/delegate that will be invoked when a message is
	 *                  received
	 * @param parameter will be passed to the delegate
	 */
	public void setReceivedRawMessageHandler(RawMessageHandler handler, Object parameter) {
		recvRawMessageHandler = handler;
		recvRawMessageHandlerParameter = parameter;
	}

	/**
	 * Sets the sent message handler for sent messages.
	 *
	 * @param handler   Handler/delegate that will be invoked when a message is
	 *                  sent<
	 * @param parameter will be passed to the delegate
	 */
	public void setSentRawMessageHandler(RawMessageHandler handler, Object parameter) {
		sentMessageHandler = handler;
		sentMessageHandlerParameter = parameter;
	}

	public void checkConnection() throws ConnectionException {
		if (running == false) {
			if (connectingException != null) {
				throw new ConnectionException(connectingException.getMessage(), connectingException);
			} else {
				throw new ConnectionException("not connected", new SocketException("10057"));
			}
		}
	}

	@Override
	public final void start() throws ConnectionException {

		connectAsync();

		while ((running == false) && (connectingException == null)) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

		if (connectingException != null) {
			throw new ConnectionException("Failed to connect: ", connectingException);
		}
	}

	private void handleConnection() {
		connecting = true;
		try {
			beforeConnection();

			running = startConnection();

			afterConnection();

			if (running) {
				loopReceiveMessage();
			}

			closeConnection();
		} catch (ConnectionException e) {
			running = false;
			connectingException = e;
			closeConnection();
		} finally {
			connecting = false;
		}
	}

	/**
	 * Do something after connection
	 */
	protected void afterConnection() {

	}

	/**
	 * Close the connection
	 */
	protected void closeConnection() {
	}

	/**
	 * Do something before connecting.
	 */
	protected void beforeConnection() {
		running = false;
		connectingException = null;
	}

	protected abstract boolean startConnection() throws ConnectionException;

	private void connectAsync() throws ConnectionException {
		if ((running == false) && (connecting == false)) {

			workerThread = new Thread(() -> handleConnection());

			workerThread.start();
		} else {
			if (running) {
				throw new ConnectionException("already connected",
						new SocketException("10056")); /* WSAEISCONN - Socket is already connected */
			} else {
				throw new ConnectionException("already connecting",
						new SocketException("10037")); /* WSAEALREADY - Operation already in progress */
			}

		}
	}

	protected final void loopReceiveMessage() {
		boolean loopRunning = running;
		while (loopRunning) {
			try {
				run();
			} catch (IOException e1) {
				loopRunning = false;
				debugLog("Run Failed: " + e1.getMessage());
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public final void stop() {
		if (running) {
			running = false;

			closeConnection();

			if (workerThread != null) {
				try {
					workerThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Run a the message receiver and state machines once. Can be used if no threads
	 * should be used.
	 */
	public abstract void run() throws IOException;
}
