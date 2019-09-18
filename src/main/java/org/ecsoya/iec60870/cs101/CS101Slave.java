//====================================================================================================
//The Free Edition of C# to Java Converter limits conversion output to 100 lines per file.

//To subscribe to the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================

package org.ecsoya.iec60870.cs101;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.CauseOfTransmission;
import org.ecsoya.iec60870.asdu.ie.ClockSynchronizationCommand;
import org.ecsoya.iec60870.asdu.ie.CounterInterrogationCommand;
import org.ecsoya.iec60870.asdu.ie.DelayAcquisitionCommand;
import org.ecsoya.iec60870.asdu.ie.InterrogationCommand;
import org.ecsoya.iec60870.asdu.ie.ReadCommand;
import org.ecsoya.iec60870.asdu.ie.ResetProcessCommand;
import org.ecsoya.iec60870.conn.FileServer;
import org.ecsoya.iec60870.conn.IMasterConnection;
import org.ecsoya.iec60870.conn.Slave;
import org.ecsoya.iec60870.layer.ISecondaryApplicationLayer;
import org.ecsoya.iec60870.layer.LinkLayer;
import org.ecsoya.iec60870.layer.LinkLayerMode;
import org.ecsoya.iec60870.layer.LinkLayerParameters;
import org.ecsoya.iec60870.layer.PrimaryLinkLayerBalanced;
import org.ecsoya.iec60870.layer.SecondaryLinkLayerBalanced;
import org.ecsoya.iec60870.layer.SecondaryLinkLayerBalanced.SecondaryLinkLayerBalancedApplicationLayer;
import org.ecsoya.iec60870.layer.SecondaryLinkLayerUnbalanced;
import org.ecsoya.iec60870.layer.SerialTransceiverFT12;
import org.ecsoya.iec60870.serial.SerialPort;
import org.ecsoya.iec60870.serial.SerialStream;

/*
 *  CS101Slave.cs
 *
 *  Copyright 2017 MZ Automation GmbH
 *
 *  This file is part of lib60870.NET
 *
 *  lib60870.NET is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib60870.NET is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with lib60870.NET.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  See COPYING file for the complete license text.
 */

/**
 * CS 101 slave implementation (implements Slave interface)
 */
public class CS101Slave extends Slave
		implements ISecondaryApplicationLayer, IMasterConnection, SecondaryLinkLayerBalancedApplicationLayer {

	private Consumer<String> DebugLog = (msg) -> {
		System.out.print("CS101 SLAVE: ");
		System.out.println(msg);
	};

	/********************************************
	 * END ISecondaryApplicationLayer
	 ********************************************/

//			private boolean sendLinkLayerTestFunction = false;

	private LinkLayer linkLayer = null;

	private byte[] buffer = new byte[300];

	private SerialPort port = null;

	private boolean running = false;

	private LinkLayerParameters linkLayerParameters;

	private LinkLayerMode linkLayerMode = LinkLayerMode.UNBALANCED;

	private PrimaryLinkLayerBalanced primaryLinkLayerBalanced = null;

	private int linkLayerAddress = 0;

	private int linkLayerAddressOtherStation; // link layer address of other station in balanced mode

	private LinkedList<BufferFrame> userDataClass1Queue = new LinkedList<BufferFrame>();
	private int userDataClass1QueueMaxSize = 100;
	private LinkedList<BufferFrame> userDataClass2Queue = new LinkedList<BufferFrame>();
	private int userDataClass2QueueMaxSize = 100;
	private SerialTransceiverFT12 transceiver;

	private FileServer fileServer;

	private boolean initialized;
	private ApplicationLayerParameters parameters = new ApplicationLayerParameters();

	/// <summary>
	/// Initializes a new instance of the <see cref="lib60870.CS101.CS101Slave"/>
	/// class.
	/// </summary>
	/// <param name="port">serial port instance</param>
	/// <param name="parameters">link layer parameters</param>
	public CS101Slave(SerialPort port, LinkLayerParameters parameters) {
		this.port = port;

		linkLayerParameters = parameters;

		if (linkLayerParameters == null)
			linkLayerParameters = new LinkLayerParameters();

		transceiver = new SerialTransceiverFT12(port, linkLayerParameters, DebugLog);

		initialized = false;

		fileServer = new FileServer(this, getAvailableFiles(), DebugLog);
	}

	/// <summary>
	/// Initializes a new instance of the <see cref="lib60870.CS101.CS101Slave"/>
	/// class.
	/// </summary>
	/// <param name="serialStream">A stream instance (e.g.
	/// TcpClientVirtualSerialPort or TcpServerVirtualSerialPort.</param>
	/// <param name="parameters">link layer parameters</param>
	public CS101Slave(SerialStream serialStream, LinkLayerParameters parameters) {
		linkLayerParameters = parameters;

		if (linkLayerParameters == null)
			linkLayerParameters = new LinkLayerParameters();

		transceiver = new SerialTransceiverFT12(serialStream, linkLayerParameters, DebugLog);

		initialized = false;

		fileServer = new FileServer(this, getAvailableFiles(), DebugLog);
	}

	public final BufferFrame DequeueUserDataClass1() {
		synchronized (userDataClass1Queue) {

			if (!userDataClass1Queue.isEmpty()) {
				return userDataClass1Queue.poll();
			} else {
				return null;
			}
		}
	}

	private BufferFrame

			DequeueUserDataClass2() {
		synchronized (userDataClass2Queue) {

			if (userDataClass2Queue.size() > 0)
				return userDataClass2Queue.pop();
			else
				return null;
		}
	}

	/**
	 * Enqueues an ASDU into the class 1 queue (for events, command responses, and
	 * other high-priority messages).
	 * 
	 * @param asdu ASDU to enqueue
	 */
	public final void EnqueueUserDataClass1(ASDU asdu) {
		synchronized (userDataClass1Queue) {

			BufferFrame frame = new BufferFrame(new byte[256], 0);

			asdu.encode(frame, parameters);

			userDataClass1Queue.offer(frame);

			while (userDataClass1Queue.size() > userDataClass1QueueMaxSize) {
				userDataClass1Queue.poll();
			}
		}
	}

	/// <summary>
	/// Enqueues an ASDU into the class 2 queue (for periodic measurments,
	/// background scan, and other low-priority data).
	/// </summary>
	/// <param name="asdu">ASDU to enqueue</param>
	public void EnqueueUserDataClass2(ASDU asdu) {
		synchronized (userDataClass2Queue) {

			BufferFrame frame = new BufferFrame(new byte[256], 0);

			asdu.encode(frame, parameters);

			userDataClass2Queue.push(frame);

			while (userDataClass2Queue.size() > userDataClass2QueueMaxSize) {
				userDataClass2Queue.pop();
			}
		}
	}

	public final ApplicationLayerParameters getApplicationLayerParameters() {
		return parameters;
	}

	public final BufferFrame getClass1Data() {
		return DequeueUserDataClass1();
	}

	public final BufferFrame getCLass2Data() {
		BufferFrame asdu = DequeueUserDataClass2();

		if (asdu == null) {
			asdu = DequeueUserDataClass1();
		}

		return asdu;
	}

	/**
	 * Gets or sets the direction bit value used for balanced mode (default is
	 * false)
	 * 
	 * <value><c>true</c> if DIR is set otherwise, <c>false</c>.</value>
	 */
	public final boolean getDIR() {
		return linkLayer.isDir();
	}

	public int getLinkLayerAddress() {
		return linkLayerAddress;
	}

	/// <summary>
	/// Gets or sets the link layer address of the other station (for balanced
	/// mode).
	/// </summary>
	/// <value>link layer address of other station.</value>
	public int getLinkLayerAddressOtherStation() {
		return linkLayerAddressOtherStation;
	}

	/**
	 * Gets or sets the link layer mode (balanced or unbalanced).
	 * 
	 * <value>The link layer mode.</value>
	 */
	public final LinkLayerMode getLinkLayerMode() {
		return this.linkLayerMode;
	}

	/**
	 * Gets or sets the application layer parameters-
	 * 
	 * Should be set before starting the communication <value>application layer
	 * parameters.</value>
	 */
	public final ApplicationLayerParameters getParameters() {
		return this.parameters;
	}

	private Function<Void, BufferFrame> getUserData() {
		return (v) -> {
			if (IsUserDataClass1Available())
				return DequeueUserDataClass1();
			else if (IsUserDataClass2Available())
				return DequeueUserDataClass2();
			else
				return null;
		};

	}

	@Override
	public boolean handle(int address, byte[] msg, int userDataStart, int userDataLength) {
		try {
			return handleApplicationLayer(address, msg, userDataStart, userDataLength);
		} catch (ASDUParsingException e) {

			e.printStackTrace();
			return false;
		}
	}

	public boolean handleApplicationLayer(int address, byte[] msg, int userDataStart, int userDataLength)
			throws ASDUParsingException {

		ASDU asdu;

		try {
			asdu = new ASDU(parameters, buffer, userDataStart, userDataStart + userDataLength);
		} catch (ASDUParsingException e) {
			DebugLog.accept("ASDU parsing failed: " + e.getMessage());
			return false;
		}

		boolean messageHandled = false;

		switch (asdu.getTypeId()) {

		case C_IC_NA_1: /* 100 - interrogation command */

			DebugLog.accept("Rcvd interrogation command C_IC_NA_1");

			if ((asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION)
					|| (asdu.getCauseOfTransmission() == CauseOfTransmission.DEACTIVATION)) {
				if (this.interrogationHandler != null) {

					InterrogationCommand irc = (InterrogationCommand) asdu.getElement(0);

					if (this.interrogationHandler.invoke(this.InterrogationHandlerParameter, this, asdu, irc.getQOI()))
						messageHandled = true;
				}
			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDU(asdu);
			}

			break;

		case C_CI_NA_1: /* 101 - counter interrogation command */

			DebugLog.accept("Rcvd counter interrogation command C_CI_NA_1");

			if ((asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION)
					|| (asdu.getCauseOfTransmission() == CauseOfTransmission.DEACTIVATION)) {
				if (this.counterInterrogationHandler != null) {

					CounterInterrogationCommand cic = (CounterInterrogationCommand) asdu.getElement(0);

					if (this.counterInterrogationHandler.invoke(this.counterInterrogationHandlerParameter, this, asdu,
							cic.getQualifier()))
						messageHandled = true;
				}
			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDU(asdu);
			}

			break;

		case C_RD_NA_1: /* 102 - read command */

			DebugLog.accept("Rcvd read command C_RD_NA_1");

			if (asdu.getCauseOfTransmission() == CauseOfTransmission.REQUEST) {

				DebugLog.accept("Read request for object: " + asdu.getCommonAddress());

				if (this.readHandler != null) {
					ReadCommand rc = (ReadCommand) asdu.getElement(0);

					if (this.readHandler.invoke(this.readHandlerParameter, this, asdu, rc.getObjectAddress()))
						messageHandled = true;

				}

			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDU(asdu);
			}

			break;

		case C_CS_NA_1: /* 103 - Clock synchronization command */

			DebugLog.accept("Rcvd clock sync command C_CS_NA_1");

			if (asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION) {

				if (this.clockSynchronizationHandler != null) {

					ClockSynchronizationCommand csc = (ClockSynchronizationCommand) asdu.getElement(0);

					if (this.clockSynchronizationHandler.invoke(this.clockSynchronizationHandlerParameter, this, asdu,
							csc.getNewTime()))
						messageHandled = true;
				}

			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDU(asdu);
			}

			break;

		case C_TS_NA_1: /* 104 - test command */

			DebugLog.accept("Rcvd test command C_TS_NA_1");

			if (asdu.getCauseOfTransmission() != CauseOfTransmission.ACTIVATION)
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
			else
				asdu.setCauseOfTransmission(CauseOfTransmission.ACTIVATION_CON);

			this.sendASDU(asdu);

			messageHandled = true;

			break;

		case C_RP_NA_1: /* 105 - Reset process command */

			DebugLog.accept("Rcvd reset process command C_RP_NA_1");

			if (asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION) {

				if (this.resetProcessHandler != null) {

					ResetProcessCommand rpc = (ResetProcessCommand) asdu.getElement(0);

					if (this.resetProcessHandler.invoke(this.resetProcessHandlerParameter, this, asdu, rpc.getQrp()))
						messageHandled = true;
				}

			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDU(asdu);
			}

			break;

		case C_CD_NA_1: /* 106 - Delay acquisition command */

			DebugLog.accept("Rcvd delay acquisition command C_CD_NA_1");

			if ((asdu.getCauseOfTransmission() == CauseOfTransmission.ACTIVATION)
					|| (asdu.getCauseOfTransmission() == CauseOfTransmission.SPONTANEOUS)) {
				if (this.delayAcquisitionHandler != null) {

					DelayAcquisitionCommand dac = (DelayAcquisitionCommand) asdu.getElement(0);

					if (this.delayAcquisitionHandler.invoke(this.delayAcquisitionHandlerParameter, this, asdu,
							dac.getDelay()))
						messageHandled = true;
				}
			} else {
				asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION);
				this.sendASDU(asdu);
			}

			break;

		}

		if (messageHandled == false)
			messageHandled = fileServer.handleFileAsdu(asdu);

		if ((messageHandled == false) && (this.asduHandler != null))
			if (this.asduHandler.invoke(this.asduHandlerParameter, this, asdu))
				messageHandled = true;

		if (messageHandled == false) {
			asdu.setCauseOfTransmission(CauseOfTransmission.UNKNOWN_TYPE_ID);
			this.sendASDU(asdu);
		}

		return true;
	}

	public final boolean handleReceivedData(byte[] msg, boolean isBroadcast, int userDataStart, int userDataLength) {
		try {
			return handleApplicationLayer(0, msg, userDataStart, userDataLength);
		} catch (ASDUParsingException e) {

			e.printStackTrace();
			return false;
		}
	}

	/********************************************
	 * ISecondaryApplicationLayer
	 ********************************************/

	public final boolean isClass1DataAvailable() {
		return IsUserDataClass1Available();
	}

	public final boolean IsUserDataClass1Available() {
		synchronized (userDataClass1Queue) {
			if (!userDataClass1Queue.isEmpty()) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Determines whether the user data class 1 queue is full.
	 * 
	 * @return <c>true</c> if the queue is full; otherwise, <c>false</c>.
	 */
	public final boolean IsUserDataClass1QueueFull() {
		return (userDataClass1Queue.size() == userDataClass1QueueMaxSize);
	}

	public final boolean IsUserDataClass2Available() {
		synchronized (userDataClass2Queue) {
			if (!userDataClass2Queue.isEmpty()) {
				return true;
			} else {
				return false;
			}
		}
	}

	/// <summary>
	/// Determines whether the user data class 2 queue is full.
	/// </summary>
	/// <returns><c>true</c> if the queue is full; otherwise,
	/// <c>false</c>.</returns>
	public boolean IsUserDataClass2QueueFull() {
		return (userDataClass2Queue.size() == userDataClass2QueueMaxSize);
	}

	/// <summary>
	/// Starts a loop that handles incoming messages.
	/// </summary>
	/// It is best to be started in a separate thread.
	/// The loop can be stopped with the Stop method.
	public void receiveMessageLoop() {
//		running = true;
//
//		if (port != null) {
//			if (port.IsOpen == false)
//				port.Open();
//
//			port.DiscardInBuffer();
//		}
//
//		while (running) {
//			Run();
//		}
//
//		if (port != null)
//			port.Close();
	}

	public final void resetCUReceived(boolean onlyFcb) {
		// TODO delete data queues
		synchronized (userDataClass1Queue) {
			userDataClass1Queue.clear();
		}
		synchronized (userDataClass2Queue) {
			userDataClass2Queue.clear();
		}
	}

	/// <summary>
	/// Run a the message receiver and state machines once. Can be used if no
	/// threads should be used.
	/// </summary>
	public void run() {
		if (initialized == false) {

			linkLayer = new LinkLayer(buffer, linkLayerParameters, transceiver, DebugLog);
			linkLayer.setLinkLayerMode(linkLayerMode);

			if (linkLayerMode == LinkLayerMode.BALANCED) {

				PrimaryLinkLayerBalanced primaryLinkLayerBalanced = new PrimaryLinkLayerBalanced(linkLayer,
						getUserData(), DebugLog);
				primaryLinkLayerBalanced.setLinkLayerAddressOtherStation(linkLayerAddressOtherStation);

				linkLayer.SetPrimaryLinkLayer(primaryLinkLayerBalanced);

				linkLayer.SetSecondaryLinkLayer(
						new SecondaryLinkLayerBalanced(linkLayer, linkLayerAddressOtherStation, this, DebugLog));
			} else {
				linkLayer.SetSecondaryLinkLayer(
						new SecondaryLinkLayerUnbalanced(linkLayer, linkLayerAddress, this, DebugLog));
			}

			initialized = true;
		}

		if (fileServer != null)
			fileServer.handleFileTransmission();

		linkLayer.run();
	}

	/********************************************
	 * IASDUSender
	 ********************************************/

	public final void sendACT_CON(ASDU asdu, boolean negative) {
		asdu.setCauseOfTransmission(CauseOfTransmission.ACTIVATION_CON);
		asdu.setNegative(negative);

		sendASDU(asdu);
	}

	public final void sendACT_TERM(ASDU asdu) {
		asdu.setCauseOfTransmission(CauseOfTransmission.ACTIVATION_TERMINATION);
		asdu.setNegative(false);

		sendASDU(asdu);
	}

	public void sendASDU(ASDU asdu) {
		EnqueueUserDataClass1(asdu);
	}

	/// <summary>
	/// Sends a link layer test function.
	/// </summary>
	public void sendLinkLayerTestFunction() {
		linkLayer.sendTestFunction();
	}

	public final void setDIR(boolean value) {
		linkLayer.setDir(value);
	}

	public void setLinkLayerAddress(int linkLayerAddress) {
		this.linkLayerAddress = linkLayerAddress;
	}

	public void setLinkLayerAddressOtherStation(int value) {
		this.linkLayerAddressOtherStation = value;
		if (primaryLinkLayerBalanced != null)
			primaryLinkLayerBalanced.setLinkLayerAddressOtherStation(value);
	}

	public final void setLinkLayerMode(LinkLayerMode value) {
		if (initialized == false) {
			linkLayerMode = value;
		}
	}

	public final void setParameters(ApplicationLayerParameters value) {
		parameters = value;
	}

	/**
	 * Sets the user data queue sizes. When the maximum size is reached the oldest
	 * value will be deleted when a new ASDU is added
	 * 
	 * @param class1QueueSize Class 1 queue size.
	 * @param class2QueueSize Class 2 queue size.
	 */
	public final void SetUserDataQueueSizes(int class1QueueSize, int class2QueueSize) {
		userDataClass1QueueMaxSize = class1QueueSize;
		userDataClass2QueueMaxSize = class2QueueSize;
	}

	/**
	 * Stops the receive message loop
	 */
	public final void Stop() {
		running = false;
	}

}