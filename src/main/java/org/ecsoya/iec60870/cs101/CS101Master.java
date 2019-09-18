//====================================================================================================
//The Free Edition of C# to Java Converter limits conversion output to 100 lines per file.

//To subscribe to the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================

package org.ecsoya.iec60870.cs101;

import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.ConnectionException;
import org.ecsoya.iec60870.RawMessageHandler;
import org.ecsoya.iec60870.asdu.ASDU;
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
import org.ecsoya.iec60870.conn.FileClient;
import org.ecsoya.iec60870.conn.IFileReceiver;
import org.ecsoya.iec60870.conn.Master;
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
import org.ecsoya.iec60870.serial.SerialPort;
import org.ecsoya.iec60870.serial.SerialStream;

public class CS101Master extends Master implements IPrimaryLinkLayerCallbacks {
	protected Thread workerThread = null;

	public LinkLayer linkLayer = null;

	public FileClient fileClient = null;

	protected SerialPort port = null;
	protected boolean running = false;

	private Consumer<String> DebugLog = (msg) -> {
		System.out.println("CS101Master: " + msg);
	};

	private void ReceiveMessageLoop() {
		running = true;

		while (running) {
			Run();

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Value of DIR bit when sending messages.
	 */
	public final boolean getDIR() {
		return linkLayer.isDir();
	}

	public final void setDIR(boolean value) {
		linkLayer.setDir(value);
	}

	/**
	 * Run the protocol state machines a single time. Alternative to Start/Stop when
	 * no background thread should be used Has to be called frequently
	 */
	public final void Run() {
		linkLayer.Run();

		if (fileClient != null) {
			fileClient.HandleFileService();
		}
	}

	/**
	 * Start a background thread running the master
	 */
	public final void Start() {
		if (port != null) {
			if (!port.isOpen()) {
				port.open();
			}

			port.discardInBuffer();
		}

		workerThread = new Thread() {
			public void run() {
				ReceiveMessageLoop();
			}
		};

		workerThread.start();
	}

	/**
	 * Stop the background thread
	 */
	public final void Stop() {
		if (running) {
			running = false;

			if (workerThread != null) {
				try {
					workerThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public final int getOwnAddress() {
		return linkLayer.getOwnAddress();
	}

	public final void setOwnAddress(int value) {
		linkLayer.setOwnAddress(value);
	}

	public final LinkLayerState GetLinkLayerState() {
		return primaryLinkLayer.GetLinkLayerState();
	}

	@Override
	public void setReceivedRawMessageHandler(RawMessageHandler handler, Object parameter) {
		linkLayer.SetReceivedRawMessageHandler(handler, parameter);
	}

	@Override
	public void setSentRawMessageHandler(RawMessageHandler handler, Object parameter) {
		linkLayer.SetSentRawMessageHandler(handler, parameter);
	}

	private PrimaryLinkLayerUnbalanced linkLayerUnbalanced = null;
	private PrimaryLinkLayerBalanced primaryLinkLayer = null;

	private SerialTransceiverFT12 transceiver;

	/* selected slave address for unbalanced mode */
	private int slaveAddress = 0;

	/* buffer to read data from serial line */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private byte[] buffer = new byte[300];
	private byte[] buffer = new byte[300];

	private LinkLayerParameters linkLayerParameters;
	private ApplicationLayerParameters appLayerParameters;

	/* user data queue for balanced mode */
	private LinkedList<BufferFrame> userDataQueue;

	private void DebugLog(String msg) {
		if (debug) {
			System.out.print("CS101 MASTER: ");
			System.out.println(msg);
		}
	}

	public CS101Master(SerialPort port, LinkLayerMode mode, LinkLayerParameters llParams) {
		this(port, mode, llParams, null);
	}

	public CS101Master(SerialPort port, LinkLayerMode mode) {
		this(port, mode, null, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CS101Master(SerialPort port, LinkLayerMode mode, LinkLayerParameters llParams = null, ApplicationLayerParameters alParams = null)
	public CS101Master(SerialPort port, LinkLayerMode mode, LinkLayerParameters llParams,
			ApplicationLayerParameters alParams) {
		if (llParams == null) {
			this.linkLayerParameters = new LinkLayerParameters();
		} else {
			this.linkLayerParameters = llParams;
		}

		if (alParams == null) {
			this.appLayerParameters = new ApplicationLayerParameters();
		} else {
			this.appLayerParameters = alParams;
		}

		this.transceiver = new SerialTransceiverFT12(port, linkLayerParameters, DebugLog);

		linkLayer = new LinkLayer(buffer, linkLayerParameters, transceiver, DebugLog);
		linkLayer.setLinkLayerMode(mode);

		if (mode == LinkLayerMode.BALANCED) {
			linkLayer.setDir(true);

			primaryLinkLayer = new PrimaryLinkLayerBalanced(linkLayer, GetUserData(), DebugLog);

			linkLayer.SetPrimaryLinkLayer(primaryLinkLayer);
			linkLayer.SetSecondaryLinkLayer(new SecondaryLinkLayerBalanced(linkLayer, 0,
					(int arg1, byte[] arg2, int arg3, int arg4) -> HandleApplicationLayer(arg1, arg2, arg3, arg4),
					DebugLog));

			userDataQueue = new LinkedList<BufferFrame>();
		} else {
			linkLayerUnbalanced = new PrimaryLinkLayerUnbalanced(linkLayer, this, DebugLog);
			linkLayer.SetPrimaryLinkLayer(linkLayerUnbalanced);
		}

		this.port = port;

		this.fileClient = null;
	}

	public CS101Master(SerialStream serialStream, LinkLayerMode mode, LinkLayerParameters llParams) {
		this(serialStream, mode, llParams, null);
	}

	public CS101Master(SerialStream serialStream, LinkLayerMode mode) {
		this(serialStream, mode, null, null);
	}

	public CS101Master(SerialStream serialStream, LinkLayerMode mode, LinkLayerParameters llParams,
			ApplicationLayerParameters alParams) {
		if (llParams == null) {
			this.linkLayerParameters = new LinkLayerParameters();
		} else {
			this.linkLayerParameters = llParams;
		}

		if (alParams == null) {
			this.appLayerParameters = new ApplicationLayerParameters();
		} else
			this.appLayerParameters = alParams;

		this.transceiver = new SerialTransceiverFT12(serialStream, linkLayerParameters, DebugLog);

		linkLayer = new LinkLayer(buffer, linkLayerParameters, transceiver, DebugLog);
		linkLayer.setLinkLayerMode(mode);

		if (mode == LinkLayerMode.BALANCED) {
			linkLayer.setDir(true);

			primaryLinkLayer = new PrimaryLinkLayerBalanced(linkLayer, GetUserData(), DebugLog);

			linkLayer.SetPrimaryLinkLayer(primaryLinkLayer);
			linkLayer.SetSecondaryLinkLayer(new SecondaryLinkLayerBalanced(linkLayer, 0,
					(int arg1, byte[] arg2, int arg3, int arg4) -> HandleApplicationLayer(arg1, arg2, arg3, arg4),
					DebugLog));

			userDataQueue = new LinkedList<BufferFrame>();
		} else {
			linkLayerUnbalanced = new PrimaryLinkLayerUnbalanced(linkLayer, this, DebugLog);
			linkLayer.SetPrimaryLinkLayer(linkLayerUnbalanced);
		}

		this.fileClient = null;
	}

	public void AddSlave(int slaveAddress) {
		linkLayerUnbalanced.AddSlaveConnection(slaveAddress);
	}

	public LinkLayerState GetLinkLayerState(int slaveAddress) {
		if (linkLayerUnbalanced != null)
			try {
				return linkLayerUnbalanced.GetStateOfSlave(slaveAddress);
			} catch (Exception e) {

				return primaryLinkLayer.GetLinkLayerState();
			}
		else
			return primaryLinkLayer.GetLinkLayerState();
	}

	public void SetLinkLayerStateChangedHandler(LinkLayerStateChanged handler, Object parameter) {
		if (linkLayerUnbalanced != null)
			linkLayerUnbalanced.SetLinkLayerStateChanged(handler, parameter);
		else
			primaryLinkLayer.SetLinkLayerStateChanged(handler, parameter);
	}

	/// <summary>
	/// Gets or sets the link layer slave address
	/// </summary>
	/// <value>Slave link layer address.</value>
	public void setSlaveAddress(int value) {
		UseSlaveAddress(value);
	}

	public int getSlaveAddress() {
		if (primaryLinkLayer == null)
			return this.slaveAddress;
		else
			return primaryLinkLayer.getLinkLayerAddressOtherStation();
	}

	/// <summary>
	/// Sets the slave link layer address to be used
	/// </summary>
	/// <param name="slaveAddress">Slave link layer address.</param>
	public void UseSlaveAddress(int slaveAddress) {
		if (primaryLinkLayer != null)
			primaryLinkLayer.setLinkLayerAddressOtherStation(slaveAddress);
		else
			this.slaveAddress = slaveAddress;
	}

	public void AccessDemand(int slaveAddress) {
		DebugLog("Access demand slave " + slaveAddress);
		linkLayerUnbalanced.RequestClass1Data(slaveAddress);
	}

	public void UserData(int slaveAddress, byte[] message, int start, int length)
			throws ConnectionException, IOException {
		DebugLog("User data slave " + slaveAddress);

		ASDU asdu;

		try {
			asdu = new ASDU(appLayerParameters, message, start, start + length);
		} catch (ASDUParsingException e) {
			DebugLog("ASDU parsing failed: " + e.getMessage());
			return;
		}

		boolean messageHandled = false;

		if (fileClient != null)
			try {
				messageHandled = fileClient.HandleFileAsdu(asdu);
			} catch (ASDUParsingException e) {
				messageHandled = false;
				e.printStackTrace();
			}

		if (messageHandled == false) {
			handleReceivedASDU(slaveAddress, asdu);
		}

	}

	public void Timeout(int slaveAddress) {
		DebugLog("Timeout accessing slave " + slaveAddress);
	}

	public void PollSingleSlave(int address) {
		try {
			linkLayerUnbalanced.RequestClass2Data(address);
		} catch (LinkLayerBusyException e) {
			DebugLog("Link layer busy");
		}
	}

	private void EnqueueUserData(ASDU asdu) {
		if (linkLayerUnbalanced != null) {
			// TODO problem -> buffer frame needs own buffer so that the message can be
			// stored.
			BufferFrame frame = new BufferFrame(buffer, 0);

			asdu.encode(frame, appLayerParameters);

			try {
				linkLayerUnbalanced.SendConfirmed(slaveAddress, frame);
			} catch (LinkLayerBusyException e) {
				e.printStackTrace();
			}
		} else {
			synchronized (userDataQueue) {

				BufferFrame frame = new BufferFrame(new byte[256], 0);

				asdu.encode(frame, appLayerParameters);

				userDataQueue.push(frame);
			}
		}
	}

	private BufferFrame DequeueUserData() {
		synchronized (userDataQueue) {

			if (userDataQueue.size() > 0)
				return userDataQueue.pop();
			else
				return null;
		}
	}

	private boolean IsUserDataAvailable() {
		synchronized (userDataQueue) {
			if (userDataQueue.size() > 0)
				return true;
			else
				return false;
		}
	}

	/// <summary>
	/// Callback function forPrimaryLinkLayerBalanced
	/// </summary>
	/// <returns>The next ASDU to send</returns>
	private Function<Void, BufferFrame> GetUserData() {
		return (v) -> {
			BufferFrame asdu = null;

			if (CS101Master.this.IsUserDataAvailable())
				return DequeueUserData();

			return asdu;

		};

	}

	/// <summary>
	/// Callback function for secondary link layer (balanced mode)
	/// </summary>
	private boolean HandleApplicationLayer(int address, byte[] msg, int userDataStart, int userDataLength) {

		try {
			ASDU asdu;

			try {
				asdu = new ASDU(appLayerParameters, buffer, userDataStart, userDataStart + userDataLength);
			} catch (ASDUParsingException e) {
				DebugLog("ASDU parsing failed: " + e.getMessage());
				return false;
			}

			boolean messageHandled = false;

			if (fileClient != null)
				try {
					messageHandled = fileClient.HandleFileAsdu(asdu);
				} catch (ASDUParsingException e) {
					messageHandled = false;
				}

			if (messageHandled == false) {
				handleReceivedASDU(address, asdu);
			}

			return messageHandled;
		} catch (ConnectionException e) {

			return false;
		}
	}

	public void SendLinkLayerTestFunction() {
		linkLayer.SendTestFunction();
	}

	public void sendInterrogationCommand(CauseOfTransmission cot, int commonAddress, byte qualifierOfInterrogation)
			throws ConnectionException {
		ASDU asdu = new ASDU(appLayerParameters, cot, false, false, (byte) appLayerParameters.getOA(), commonAddress,
				false);

		asdu.addInformationObject(new InterrogationCommand(0, qualifierOfInterrogation));

		EnqueueUserData(asdu);
	}

	public void sendCounterInterrogationCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			byte qualifierOfCounter) throws ConnectionException {
		ASDU asdu = new ASDU(appLayerParameters, causeOfTransmission, false, false, (byte) appLayerParameters.getOA(),
				commonAddress, false);

		asdu.addInformationObject(new CounterInterrogationCommand(0, qualifierOfCounter));

		EnqueueUserData(asdu);
	}

	public void sendReadCommand(int commonAddress, int informationObjectAddress) throws ConnectionException {
		ASDU asdu = new ASDU(appLayerParameters, CauseOfTransmission.REQUEST, false, false,
				(byte) appLayerParameters.getOA(), commonAddress, false);

		asdu.addInformationObject(new ReadCommand(informationObjectAddress));

		EnqueueUserData(asdu);
	}

	public void sendClockSyncCommand(int commonAddress, CP56Time2a time) throws ConnectionException {
		ASDU asdu = new ASDU(appLayerParameters, CauseOfTransmission.ACTIVATION, false, false,
				(byte) appLayerParameters.getOA(), commonAddress, false);

		asdu.addInformationObject(new ClockSynchronizationCommand(0, time));

		EnqueueUserData(asdu);
	}

	public void sendTestCommand(int commonAddress) throws ConnectionException {
		ASDU asdu = new ASDU(appLayerParameters, CauseOfTransmission.ACTIVATION, false, false,
				(byte) appLayerParameters.getOA(), commonAddress, false);

		asdu.addInformationObject(new TestCommand());

		EnqueueUserData(asdu);
	}

	public void sendTestCommandWithCP56Time2a(int commonAddress, short testSequenceNumber, CP56Time2a timestamp)
			throws ConnectionException {
		ASDU asdu = new ASDU(appLayerParameters, CauseOfTransmission.ACTIVATION, false, false,
				(byte) appLayerParameters.getOA(), commonAddress, false);

		asdu.addInformationObject(new TestCommandWithCP56Time2a(testSequenceNumber, timestamp));

		EnqueueUserData(asdu);
	}

	public void sendResetProcessCommand(CauseOfTransmission causeOfTransmission, int commonAddress, byte qualifier)
			throws ConnectionException {
		ASDU asdu = new ASDU(appLayerParameters, CauseOfTransmission.ACTIVATION, false, false,
				(byte) appLayerParameters.getOA(), commonAddress, false);

		asdu.addInformationObject(new ResetProcessCommand(0, qualifier));

		EnqueueUserData(asdu);
	}

	public void sendDelayAcquisitionCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			CP16Time2a delay) throws ConnectionException {
		ASDU asdu = new ASDU(appLayerParameters, CauseOfTransmission.ACTIVATION, false, false,
				(byte) appLayerParameters.getOA(), commonAddress, false);

		asdu.addInformationObject(new DelayAcquisitionCommand(0, delay));

		EnqueueUserData(asdu);
	}

	public void sendControlCommand(CauseOfTransmission causeOfTransmission, int commonAddress,
			InformationObject informationObject) throws ConnectionException {
		ASDU controlCommand = new ASDU(appLayerParameters, causeOfTransmission, false, false,
				(byte) appLayerParameters.getOA(), commonAddress, false);

		controlCommand.addInformationObject(informationObject);

		EnqueueUserData(controlCommand);
	}

	public void sendASDU(ASDU asdu) throws ConnectionException {
		EnqueueUserData(asdu);
	}

	public ApplicationLayerParameters

			getApplicationLayerParameters() {
		return appLayerParameters;
	}

	public void getFile(int commonAddress, int informationObjectAddress, NameOfFile nameOfFile, IFileReceiver receiver)
			throws ConnectionException {
		if (fileClient == null)
			fileClient = new FileClient(this, DebugLog);

		fileClient.RequestFile(commonAddress, informationObjectAddress, nameOfFile, receiver);
	}
}