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

package org.ecsoya.iec60870.cs101;

import java.io.IOException;
import java.util.LinkedList;

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
import org.ecsoya.iec60870.asdu.ie.InterrogationCommand;
import org.ecsoya.iec60870.asdu.ie.ReadCommand;
import org.ecsoya.iec60870.asdu.ie.ResetProcessCommand;
import org.ecsoya.iec60870.asdu.ie.TestCommand;
import org.ecsoya.iec60870.asdu.ie.TestCommandWithCP56Time2a;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;
import org.ecsoya.iec60870.core.ConnectionException;
import org.ecsoya.iec60870.core.Master;
import org.ecsoya.iec60870.core.file.FileClient;
import org.ecsoya.iec60870.core.file.IFileReceiver;
import org.ecsoya.iec60870.core.handler.RawMessageHandler;
import org.ecsoya.iec60870.layer.IPrimaryLinkLayerCallbacks;
import org.ecsoya.iec60870.layer.LinkLayer;
import org.ecsoya.iec60870.layer.LinkLayerMode;
import org.ecsoya.iec60870.layer.LinkLayerParameters;
import org.ecsoya.iec60870.layer.LinkLayerState;
import org.ecsoya.iec60870.layer.LinkLayerStateChanged;
import org.ecsoya.iec60870.layer.PrimaryLinkLayer.LinkLayerBusyException;
import org.ecsoya.iec60870.layer.PrimaryLinkLayerBalanced;
import org.ecsoya.iec60870.layer.PrimaryLinkLayerUnbalanced;
import org.ecsoya.iec60870.layer.SecondaryLinkLayerBalanced;
import org.ecsoya.iec60870.layer.SerialTransceiverFT12;
import org.ecsoya.iec60870.serial.SerialStream;

public class CS101Master extends Master implements IPrimaryLinkLayerCallbacks {

	private LinkLayer linkLayer = null;

	private FileClient fileClient = null;

	private PrimaryLinkLayerUnbalanced linkLayerUnbalanced = null;

	private PrimaryLinkLayerBalanced primaryLinkLayer = null;

	private final SerialTransceiverFT12 transceiver;

	/* selected slave address for unbalanced mode */
	private int slaveAddress = 0;

	/* buffer to read data from serial line */
	private byte[] buffer = new byte[300];

	private LinkLayerParameters linkLayerParameters;

	/* user data queue for balanced mode */
	private LinkedList<BufferFrame> userDataQueue;

	public CS101Master(SerialStream serialStream, LinkLayerMode mode) {
		this(serialStream, mode, new LinkLayerParameters(), new ApplicationLayerParameters());
	}

	public CS101Master(SerialStream serialStream, LinkLayerMode mode, LinkLayerParameters llParams) {
		this(serialStream, mode, llParams, new ApplicationLayerParameters());
	}

	public CS101Master(SerialStream serialStream, LinkLayerMode mode, LinkLayerParameters llParams,
			ApplicationLayerParameters alParams) {
		super(alParams);
		if (llParams == null) {
			this.linkLayerParameters = new LinkLayerParameters();
		} else {
			this.linkLayerParameters = llParams;
		}

		this.transceiver = new SerialTransceiverFT12(serialStream, linkLayerParameters, (msg) -> debugLog(msg));

		setupLinkLayer(mode);

		this.fileClient = null;
	}

	private void setupLinkLayer(LinkLayerMode mode) {
		linkLayer = new LinkLayer(buffer, linkLayerParameters, transceiver, (msg) -> debugLog(msg));
		linkLayer.setLinkLayerMode(mode);

		if (mode == LinkLayerMode.BALANCED) {
			linkLayer.setDir(true);

			primaryLinkLayer = new PrimaryLinkLayerBalanced(linkLayer, () -> getUserData(), (msg) -> debugLog(msg));

			linkLayer.setPrimaryLinkLayer(primaryLinkLayer);
			linkLayer.setSecondaryLinkLayer(new SecondaryLinkLayerBalanced(linkLayer, 0,
					(int arg1, byte[] arg2, int arg3, int arg4) -> handleApplicationLayer(arg1, arg2, arg3, arg4),
					(msg) -> debugLog(msg)));

			userDataQueue = new LinkedList<BufferFrame>();
		} else {
			linkLayerUnbalanced = new PrimaryLinkLayerUnbalanced(linkLayer, this, (msg) -> debugLog(msg));
			linkLayer.setPrimaryLinkLayer(linkLayerUnbalanced);
		}
	}

	@Override
	public void handleAccessDemand(int slaveAddress) {
		debugLog("Access demand slave " + slaveAddress);
		linkLayerUnbalanced.requestClass1Data(slaveAddress);
	}

	public void addSlave(int slaveAddress) {
		linkLayerUnbalanced.addSlaveConnection(slaveAddress);
	}

	protected void debugLog(String msg) {
		if (isDebugLog()) {
			System.out.print("CS101 MASTER: ");
		}
		super.debugLog(msg);
	}

	private BufferFrame dequeueUserData() {
		synchronized (userDataQueue) {

			if (userDataQueue.size() > 0) {
				return userDataQueue.pop();
			} else {
				return null;
			}
		}
	}

	private void enqueueUserData(ASDU asdu) {
		if (linkLayerUnbalanced != null) {
			// TODO problem -> buffer frame needs own buffer so that the message can be
			// stored.
			BufferFrame frame = new BufferFrame(buffer, 0);

			asdu.encode(frame, getApplicationLayerParameters());

			try {
				linkLayerUnbalanced.sendConfirmed(slaveAddress, frame);
			} catch (LinkLayerBusyException e) {
				e.printStackTrace();
			}
		} else {
			synchronized (userDataQueue) {

				BufferFrame frame = new BufferFrame(new byte[256], 0);

				asdu.encode(frame, getApplicationLayerParameters());

				userDataQueue.push(frame);
			}
		}
	}

	/**
	 * Value of DIR bit when sending messages.
	 */
	public final boolean getDIR() {
		return linkLayer.isDir();
	}

	@Override
	public void getFile(int commonAddress, int informationObjectAddress, NameOfFile nameOfFile, IFileReceiver receiver)
			throws ConnectionException {
		if (fileClient == null) {
			fileClient = new FileClient(this, (msg) -> debugLog(msg));
		}

		fileClient.requestFile(commonAddress, informationObjectAddress, nameOfFile, receiver);
	}

	public final LinkLayerState getLinkLayerState() {
		return primaryLinkLayer.getLinkLayerState();
	}

	public LinkLayerState getLinkLayerState(int slaveAddress) {
		if (linkLayerUnbalanced != null) {
			try {
				return linkLayerUnbalanced.getStateOfSlave(slaveAddress);
			} catch (Exception e) {

				return primaryLinkLayer.getLinkLayerState();
			}
		} else {
			return primaryLinkLayer.getLinkLayerState();
		}
	}

	public final int getOwnAddress() {
		return linkLayer.getOwnAddress();
	}

	public int getSlaveAddress() {
		if (primaryLinkLayer == null) {
			return this.slaveAddress;
		} else {
			return primaryLinkLayer.getLinkLayerAddressOtherStation();
		}
	}

	/// <summary>
	/// Callback function forPrimaryLinkLayerBalanced
	/// </summary>
	/// <returns>The next ASDU to send</returns>
	private BufferFrame getUserData() {
		BufferFrame asdu = null;

		if (CS101Master.this.isUserDataAvailable()) {
			return dequeueUserData();
		}

		return asdu;

	}

	/// <summary>
	/// Callback function for secondary link layer (balanced mode)
	/// </summary>
	private boolean handleApplicationLayer(int address, byte[] msg, int userDataStart, int userDataLength) {

		try {
			ASDU asdu;

			try {
				asdu = new ASDU(getApplicationLayerParameters(), buffer, userDataStart, userDataStart + userDataLength);
			} catch (ASDUParsingException e) {
				debugLog("ASDU parsing failed: " + e.getMessage());
				return false;
			}

			boolean messageHandled = false;

			if (fileClient != null) {
				try {
					messageHandled = fileClient.handleFileAsdu(asdu);
				} catch (ASDUParsingException e) {
					messageHandled = false;
				}
			}

			if (messageHandled == false) {
				handleReceivedASDU(address, asdu);
			}

			return messageHandled;
		} catch (ConnectionException e) {

			return false;
		}
	}

	@Override
	public void handleUserData(int slaveAddress, byte[] message, int start, int length)
			throws ConnectionException, IOException {
		debugLog("User data slave " + slaveAddress);

		ASDU asdu;

		try {
			asdu = new ASDU(getApplicationLayerParameters(), message, start, start + length);
		} catch (ASDUParsingException e) {
			debugLog("ASDU parsing failed: " + e.getMessage());
			return;
		}

		boolean messageHandled = false;

		if (fileClient != null) {
			try {
				messageHandled = fileClient.handleFileAsdu(asdu);
			} catch (ASDUParsingException e) {
				messageHandled = false;
				e.printStackTrace();
			}
		}

		if (messageHandled == false) {
			handleReceivedASDU(slaveAddress, asdu);
		}

	}

	private boolean isUserDataAvailable() {
		synchronized (userDataQueue) {
			if (userDataQueue.size() > 0) {
				return true;
			} else {
				return false;
			}
		}
	}

	public void pollSingleSlave(int address) {
		try {
			linkLayerUnbalanced.requestClass2Data(address);
		} catch (LinkLayerBusyException e) {
			debugLog("Link layer busy");
		}
	}

	public final void run() throws IOException {
		linkLayer.run();

		if (fileClient != null) {
			fileClient.handleFileService();
		}
	}

	@Override
	public void sendASDU(ASDU asdu) throws ConnectionException {
		enqueueUserData(asdu);
	}

	@Override
	public void sendClockSyncCommand(int commonAddress, CP56Time2a time) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new ClockSynchronizationCommand(0, time));

		enqueueUserData(asdu);
	}

	@Override
	public void sendControlCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			InformationObject informationObject) throws ConnectionException {
		ASDU controlCommand = new ASDU(getApplicationLayerParameters(), causeOfTransmission, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		controlCommand.addInformationObject(informationObject);

		enqueueUserData(controlCommand);
	}

	@Override
	public void sendCounterInterrogationCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			byte qualifierOfCounter) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), causeOfTransmission, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new CounterInterrogationCommand(0, qualifierOfCounter));

		enqueueUserData(asdu);
	}

	@Override
	public void sendDelayAcquisitionCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			CP16Time2a delay) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new DelayAcquisitionCommand(0, delay));

		enqueueUserData(asdu);
	}

	@Override
	public void sendInterrogationCommand(CauseOfTransmission cot, int commonAddress, byte qualifierOfInterrogation)
			throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), cot, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new InterrogationCommand(0, qualifierOfInterrogation));

		enqueueUserData(asdu);
	}

	public void sendLinkLayerTestFunction() {
		linkLayer.sendTestFunction();
	}

	@Override
	public void sendReadCommand(int commonAddress, int informationObjectAddress) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.REQUEST, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new ReadCommand(informationObjectAddress));

		enqueueUserData(asdu);
	}

	@Override
	public void sendResetProcessCommand(CauseOfTransmission causeOfTransmission, int commonAddress, byte qualifier)
			throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new ResetProcessCommand(0, qualifier));

		enqueueUserData(asdu);
	}

	@Override
	public void sendTestCommand(int commonAddress) throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new TestCommand());

		enqueueUserData(asdu);
	}

	@Override
	public void sendTestCommandWithCP56Time2a(int commonAddress, short testSequenceNumber, CP56Time2a timestamp)
			throws ConnectionException {
		ASDU asdu = new ASDU(getApplicationLayerParameters(), CauseOfTransmission.ACTIVATION, false, false,
				(byte) getApplicationLayerParameters().getOA(), commonAddress, false);

		asdu.addInformationObject(new TestCommandWithCP56Time2a(testSequenceNumber, timestamp));

		enqueueUserData(asdu);
	}

	public final void setDIR(boolean value) {
		linkLayer.setDir(value);
	}

	public void setLinkLayerStateChangedHandler(LinkLayerStateChanged handler, Object parameter) {
		if (linkLayerUnbalanced != null) {
			linkLayerUnbalanced.setLinkLayerStateChanged(handler, parameter);
		} else {
			primaryLinkLayer.setLinkLayerStateChanged(handler, parameter);
		}
	}

	public final void setOwnAddress(int value) {
		linkLayer.setOwnAddress(value);
	}

	@Override
	public void setReceivedRawMessageHandler(RawMessageHandler handler, Object parameter) {
		linkLayer.setReceivedRawMessageHandler(handler, parameter);
	}

	@Override
	public void setSentRawMessageHandler(RawMessageHandler handler, Object parameter) {
		linkLayer.setSentRawMessageHandler(handler, parameter);
	}

	/// <summary>
	/// Gets or sets the link layer slave address
	/// </summary>
	/// <value>Slave link layer address.</value>
	public void setSlaveAddress(int value) {
		this.slaveAddress = value;

		if (primaryLinkLayer != null) {
			primaryLinkLayer.setLinkLayerAddressOtherStation(slaveAddress);
		}
	}

	@Override
	protected boolean startConnection() throws ConnectionException {
		try {
			this.transceiver.connect();
			return true;
		} catch (IOException e) {
			throw new ConnectionException("Failed to connect: ", e);
		}
	}

	@Override
	protected void closeConnection() {
		super.closeConnection();
		try {
			this.transceiver.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleTimeout(int slaveAddress) {
		debugLog("Timeout accessing slave " + slaveAddress);
	}

}
