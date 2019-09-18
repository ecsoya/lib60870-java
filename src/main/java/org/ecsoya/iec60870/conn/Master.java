package org.ecsoya.iec60870.conn;

import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.ConnectionException;
import org.ecsoya.iec60870.RawMessageHandler;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ASDUReceivedHandler;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

/**
 * Common interface for CS104 and CS101 balanced and unbalanced master
 */
public abstract class Master {

	protected boolean debug;

	private RawMessageHandler recvRawMessageHandler = null;
	private Object recvRawMessageHandlerParameter = null;

	private RawMessageHandler sentMessageHandler = null;
	private Object sentMessageHandlerParameter = null;

	private ASDUReceivedHandler asduReceivedHandler = null;
	private Object asduReceivedHandlerParameter = null;

	/**
	 * Get the application layer parameters used by this master instance
	 * 
	 * @return used application layer parameters
	 */
	public abstract ApplicationLayerParameters getApplicationLayerParameters();

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

	public final boolean isDebug() {
		return this.debug;
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
		debug = value;
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
}