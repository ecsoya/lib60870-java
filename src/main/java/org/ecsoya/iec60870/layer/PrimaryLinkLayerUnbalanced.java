/**
 * 
 */
package org.ecsoya.iec60870.layer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.ecsoya.iec60870.BufferFrame;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class PrimaryLinkLayerUnbalanced extends PrimaryLinkLayer implements IPrimaryLinkLayerUnbalanced {

	private LinkLayer linkLayer;
	private Consumer<String> debugLog;

	// private boolean waitingForResponse = false;

	private List<SlaveConnection> slaveConnections;

	/// <summary>
	/// The current active slave connection.
	/// </summary>
	private SlaveConnection currentSlave = null;

	private BufferFrame nextBroadcastMessage = null;

	private IPrimaryLinkLayerCallbacks callbacks = null;

	private LinkLayerStateChanged stateChanged = null;
	private Object stateChangedParameter = null;

	// can this class implement Master interface?
	private class SlaveConnection {

		private Consumer<String> debugLog = null;

		public int address;
		public PrimaryLinkLayerState primaryState = PrimaryLinkLayerState.IDLE;
		public long lastSendTime = 0;
		public long originalSendTime = 0;
		public boolean nextFcb = true;
		public boolean waitingForResponse = false;
		public LinkLayerState linkLayerState = LinkLayerState.IDLE;

		PrimaryLinkLayerUnbalanced linkLayerUnbalanced;

		private boolean sendLinkLayerTestFunction = false;

		// don't send new application layer messages to avoid data flow congestion
		private boolean dontSendMessages = false;

		public BufferFrame nextMessage = null;
		private BufferFrame lastSentASDU = null;

		public boolean requireConfirmation = false;

		public boolean resetCu = false;
		public boolean requestClass2Data = false;
		public boolean requestClass1Data = false;

		private LinkLayer linkLayer;

		private void SetState(LinkLayerState newState) {
			if (linkLayerState != newState) {

				linkLayerState = newState;

				if (linkLayerUnbalanced.stateChanged != null)
					linkLayerUnbalanced.stateChanged.performStateChanged(linkLayerUnbalanced.stateChangedParameter,
							address, newState);
			}
		}

		public SlaveConnection(int address, LinkLayer linkLayer, Consumer<String> debugLog,
				PrimaryLinkLayerUnbalanced linkLayerUnbalanced) {
			this.address = address;
			this.linkLayer = linkLayer;
			this.debugLog = debugLog;
			this.linkLayerUnbalanced = linkLayerUnbalanced;
		}

		public boolean IsMessageWaitingToSend() {
			if (requestClass1Data || requestClass2Data || (nextMessage != null))
				return true;
			else
				return false;
		}

		void HandleMessage(FunctionCodeSecondary fcs, boolean acd, boolean dfc, int address, byte[] msg,
				int userDataStart, int userDataLength) throws Exception {
			PrimaryLinkLayerState newState = primaryState;

			if (dfc) {

				// stop sending ASDUs; only send Status of link requests
				dontSendMessages = true;

				switch (primaryState) {
				case EXECUTE_REQUEST_STATUS_OF_LINK:
				case EXECUTE_RESET_REMOTE_LINK:
					newState = PrimaryLinkLayerState.EXECUTE_REQUEST_STATUS_OF_LINK;
					break;
				case EXECUTE_SERVICE_SEND_CONFIRM:
					// TODO message must be handled and switched to BUSY state later!
				case SECONDARY_LINK_LAYER_BUSY:
					newState = PrimaryLinkLayerState.SECONDARY_LINK_LAYER_BUSY;
					break;
				default:
					break;
				}

				SetState(LinkLayerState.BUSY);

				primaryState = newState;
				return;

			} else {
				// unblock transmission of application layer messages
				dontSendMessages = false;
			}

			switch (fcs) {

			case ACK:

				DebugLog("PLL - received ACK");

				if (primaryState == PrimaryLinkLayerState.EXECUTE_RESET_REMOTE_LINK) {
					newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;

					SetState(LinkLayerState.AVAILABLE);
				} else if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM) {

					if (sendLinkLayerTestFunction)
						sendLinkLayerTestFunction = false;

					SetState(LinkLayerState.AVAILABLE);

					newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				} else if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_REQUEST_RESPOND) {

					/* single char ACK is interpreted as RESP NO DATA */
					requestClass1Data = false;
					requestClass2Data = false;

					SetState(LinkLayerState.AVAILABLE);

					newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				}

				waitingForResponse = false;
				break;

			case NACK:

				DebugLog("PLL - received NACK");

				if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM) {

					SetState(LinkLayerState.BUSY);

					newState = PrimaryLinkLayerState.SECONDARY_LINK_LAYER_BUSY;
				}

				break;

			case STATUS_OF_LINK_OR_ACCESS_DEMAND:

				DebugLog("PLL - received STATUS OF LINK");

				if (primaryState == PrimaryLinkLayerState.EXECUTE_REQUEST_STATUS_OF_LINK) {

					DebugLog("PLL - SEND RESET REMOTE LINK");

					linkLayer.SendFixedFramePrimary(FunctionCodePrimary.RESET_REMOTE_LINK, address, false, false);

					lastSendTime = System.currentTimeMillis();
					waitingForResponse = true;
					newState = PrimaryLinkLayerState.EXECUTE_RESET_REMOTE_LINK;

					SetState(LinkLayerState.BUSY);
				} else { /* illegal message */
					newState = PrimaryLinkLayerState.IDLE;

					SetState(LinkLayerState.ERROR);
				}

				break;

			case RESP_USER_DATA:

				DebugLog("PLL - received USER DATA");

				if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_REQUEST_RESPOND) {
					linkLayerUnbalanced.callbacks.UserData(address, msg, userDataStart, userDataLength);

					requestClass1Data = false;
					requestClass2Data = false;

					newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;

					SetState(LinkLayerState.AVAILABLE);
				} else { /* illegal message */
					newState = PrimaryLinkLayerState.IDLE;

					SetState(LinkLayerState.ERROR);
				}

				break;

			case RESP_NACK_NO_DATA:

				DebugLog("PLL - received RESP NO DATA");

				if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_REQUEST_RESPOND) {
					newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;

					requestClass1Data = false;
					requestClass2Data = false;

					SetState(LinkLayerState.AVAILABLE);
				} else { /* illegal message */
					newState = PrimaryLinkLayerState.IDLE;

					SetState(LinkLayerState.ERROR);
				}

				break;

			case LINK_SERVICE_NOT_FUNCTIONING:
			case LINK_SERVICE_NOT_IMPLEMENTED:

				DebugLog("PLL - link layer service not functioning/not implemented in secondary station ");

				if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM) {
					newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;

					SetState(LinkLayerState.AVAILABLE);
				}

				break;

			default:
				DebugLog("UNEXPECTED SECONDARY LINK LAYER MESSAGE");
				break;
			}

			if (acd) {
				if (linkLayerUnbalanced.callbacks != null)
					linkLayerUnbalanced.callbacks.AccessDemand(address);
			}

			DebugLog("PLL RECV - old state: " + primaryState + " new state: " + newState);

			primaryState = newState;
		}

		public void RunStateMachine() throws IOException {
			PrimaryLinkLayerState newState = primaryState;

			switch (primaryState) {

			case IDLE:

				waitingForResponse = false;
				originalSendTime = 0;
				lastSendTime = 0;
				sendLinkLayerTestFunction = false;
				newState = PrimaryLinkLayerState.EXECUTE_REQUEST_STATUS_OF_LINK;

				break;

			case EXECUTE_REQUEST_STATUS_OF_LINK:

				if (waitingForResponse) {

					if (System.currentTimeMillis() > (lastSendTime + linkLayer.getTimeoutForACK())) {
						linkLayer.SendFixedFramePrimary(FunctionCodePrimary.REQUEST_LINK_STATUS, address, false, false);

						lastSendTime = System.currentTimeMillis();
					}

				} else {

					DebugLog("PLL - SEND RESET REMOTE LINK");

					linkLayer.SendFixedFramePrimary(FunctionCodePrimary.RESET_REMOTE_LINK, address, false, false);

					lastSendTime = System.currentTimeMillis();
					waitingForResponse = true;
					newState = PrimaryLinkLayerState.EXECUTE_RESET_REMOTE_LINK;
				}

				break;

			case EXECUTE_RESET_REMOTE_LINK:

				if (waitingForResponse) {
					if (System.currentTimeMillis() > (lastSendTime + linkLayer.getTimeoutForACK())) {
						waitingForResponse = false;
						newState = PrimaryLinkLayerState.IDLE;

						SetState(LinkLayerState.ERROR);
					}
				} else {
					newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;

					SetState(LinkLayerState.AVAILABLE);
				}

				break;

			case LINK_LAYERS_AVAILABLE:

				if (sendLinkLayerTestFunction) {
					DebugLog("PLL - SEND TEST LINK");

					linkLayer.SendFixedFramePrimary(FunctionCodePrimary.TEST_FUNCTION_FOR_LINK, address, nextFcb, true);

					nextFcb = !nextFcb;
					lastSendTime = System.currentTimeMillis();
					originalSendTime = lastSendTime;
					waitingForResponse = true;

					newState = PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM;
				} else if (requestClass1Data || requestClass2Data) {

					if (requestClass1Data) {
						DebugLog("PLL - SEND FC 10 - REQ UD 1");

						linkLayer.SendFixedFramePrimary(FunctionCodePrimary.REQUEST_USER_DATA_CLASS_1, address, nextFcb,
								true);
					} else {
						DebugLog("PLL - SEND FC 11 - REQ UD 2");

						linkLayer.SendFixedFramePrimary(FunctionCodePrimary.REQUEST_USER_DATA_CLASS_2, address, nextFcb,
								true);
					}

					nextFcb = !nextFcb;
					lastSendTime = System.currentTimeMillis();
					originalSendTime = lastSendTime;
					waitingForResponse = true;
					newState = PrimaryLinkLayerState.EXECUTE_SERVICE_REQUEST_RESPOND;
				} else {
					BufferFrame asdu = nextMessage;

					if (asdu != null) {

						DebugLog("PLL - SEND FC 03 - USER DATA CONFIRMED");

						linkLayer.SendVariableLengthFramePrimary(FunctionCodePrimary.USER_DATA_CONFIRMED, address,
								nextFcb, true, asdu);

						lastSentASDU = nextMessage;
						nextMessage = null;

						nextFcb = !nextFcb;

						lastSendTime = System.currentTimeMillis();
						originalSendTime = lastSendTime;
						waitingForResponse = true;

						newState = PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM;
					}
				}

				break;

			case EXECUTE_SERVICE_SEND_CONFIRM:

				if (System.currentTimeMillis() > (lastSendTime + linkLayer.getTimeoutForACK())) {

					if (System.currentTimeMillis() > (originalSendTime + linkLayer.getTimeoutRepeat())) {
						DebugLog("TIMEOUT: ASDU not confirmed after repeated transmission");
						newState = PrimaryLinkLayerState.IDLE;

						SetState(LinkLayerState.ERROR);
					} else {
						DebugLog("TIMEOUT: 1 ASDU not confirmed");

						if (sendLinkLayerTestFunction) {

							DebugLog("PLL - SEND FC 02 - RESET REMOTE LINK [REPEAT]");

							linkLayer.SendFixedFramePrimary(FunctionCodePrimary.TEST_FUNCTION_FOR_LINK, address,
									!nextFcb, true);

						} else {

							DebugLog("PLL - SEND FC 03 - USER DATA CONFIRMED [REPEAT]");

							linkLayer.SendVariableLengthFramePrimary(FunctionCodePrimary.USER_DATA_CONFIRMED, address,
									!nextFcb, true, lastSentASDU);

						}

						lastSendTime = System.currentTimeMillis();
					}
				}

				break;

			case EXECUTE_SERVICE_REQUEST_RESPOND:

				if (System.currentTimeMillis() > (lastSendTime + linkLayer.getTimeoutForACK())) {

					if (System.currentTimeMillis() > (originalSendTime + linkLayer.getTimeoutRepeat())) {
						DebugLog("TIMEOUT: ASDU not confirmed after repeated transmission");

						newState = PrimaryLinkLayerState.IDLE;
						requestClass1Data = false;
						requestClass2Data = false;

						SetState(LinkLayerState.ERROR);
					} else {
						DebugLog("TIMEOUT: ASDU not confirmed");

						if (requestClass1Data) {
							DebugLog("PLL - SEND FC 10 - REQ UD 1 [REPEAT]");

							linkLayer.SendFixedFramePrimary(FunctionCodePrimary.REQUEST_USER_DATA_CLASS_1, address,
									!nextFcb, true);
						} else if (requestClass2Data) {

							DebugLog("PLL - SEND FC 11 - REQ UD 2 [REPEAT]");

							linkLayer.SendFixedFramePrimary(FunctionCodePrimary.REQUEST_USER_DATA_CLASS_2, address,
									!nextFcb, true);
						}

						lastSendTime = System.currentTimeMillis();
					}
				}

				break;

			case SECONDARY_LINK_LAYER_BUSY:
				// TODO - reject new requests from application layer?
				break;

			}

			if (primaryState != newState)
				DebugLog("PLL - old state: " + primaryState + " new state: " + newState);

			primaryState = newState;

		}

		private void DebugLog(String log) {
			if (debugLog != null) {
				debugLog.accept(log);
			}
		}
	}

	/********************************
	 * IPrimaryLinkLayerUnbalanced
	 ********************************/

	public void ResetCU(int slaveAddress) {
		SlaveConnection slave = GetSlaveConnection(slaveAddress);

		if (slave != null)
			slave.resetCu = true;
	}

	public boolean IsChannelAvailable(int slaveAddress) {
		SlaveConnection slave = GetSlaveConnection(slaveAddress);

		if (slave != null) {
			if (slave.IsMessageWaitingToSend() == false)
				return true;
		}

		return false;
	}

	public void RequestClass1Data(int slaveAddress) {
		SlaveConnection slave = GetSlaveConnection(slaveAddress);

		if (slave != null) {
			slave.requestClass1Data = true;
			;
		}
	}

	public void RequestClass2Data(int slaveAddress) throws LinkLayerBusyException {
		SlaveConnection slave = GetSlaveConnection(slaveAddress);

		if (slave != null) {
			if (slave.IsMessageWaitingToSend())
				throw new LinkLayerBusyException("Message pending");
			else
				slave.requestClass2Data = true;
		}
	}

	public void SendConfirmed(int slaveAddress, BufferFrame message) throws LinkLayerBusyException {
		SlaveConnection slave = GetSlaveConnection(slaveAddress);

		if (slave != null) {
			if (slave.nextMessage != null)
				throw new LinkLayerBusyException("Message pending");
			else {
				slave.nextMessage = message.clone();
				slave.requireConfirmation = true;
			}
		}
	}

	public void SendNoReply(int slaveAddress, BufferFrame message) throws LinkLayerBusyException {
		if (slaveAddress == linkLayer.GetBroadcastAddress()) {
			if (nextBroadcastMessage != null)
				throw new LinkLayerBusyException("Broadcast message pending");
			else
				nextBroadcastMessage = message;
		} else {
			SlaveConnection slave = GetSlaveConnection(slaveAddress);

			if (slave != null) {
				if (slave.IsMessageWaitingToSend())
					throw new LinkLayerBusyException("Message pending");
				else {
					slave.nextMessage = message;
					slave.requireConfirmation = false;
				}
			}
		}
	}

	/********************************
	 * END IPrimaryLinkLayerUnbalanced
	 ********************************/

	public PrimaryLinkLayerUnbalanced(LinkLayer linkLayer, IPrimaryLinkLayerCallbacks callbacks,
			Consumer<String> debugLog) {
		this.linkLayer = linkLayer;
		this.callbacks = callbacks;
		this.debugLog = debugLog;
		this.slaveConnections = new ArrayList<SlaveConnection>();
	}

	private SlaveConnection GetSlaveConnection(int slaveAddres) {
		for (SlaveConnection connection : slaveConnections) {
			if (connection.address == slaveAddres)
				return connection;
		}

		return null;
	}

	public void AddSlaveConnection(int slaveAddress) {
		SlaveConnection slave = GetSlaveConnection(slaveAddress);

		if (slave == null)
			slaveConnections.add(new SlaveConnection(slaveAddress, linkLayer, debugLog, this));
	}

	public LinkLayerState GetStateOfSlave(int slaveAddress) throws Exception {
		SlaveConnection connection = GetSlaveConnection(slaveAddress);

		if (connection != null)
			return connection.linkLayerState;
		else
			throw new Exception("No slave with this address found");
	}

	public void HandleMessage(FunctionCodeSecondary fcs, boolean acd, boolean dfc, int address, byte[] msg,
			int userDataStart, int userDataLength) throws Exception {
		SlaveConnection slave = null;

		if (address == -1)
			slave = currentSlave;
		else
			slave = GetSlaveConnection(address);

		if (slave != null) {

			slave.HandleMessage(fcs, acd, dfc, address, msg, userDataStart, userDataLength);

		} else {
			DebugLog("PLL RECV - response from unknown slave " + address + " !");
		}
	}

	private int currentSlaveIndex = 0;

	public void RunStateMachine() throws IOException {
		// run all the link layer state machines for the registered slaves

		if (slaveConnections.size() > 0) {

			if (currentSlave == null) {

				/* schedule next slave connection */
				currentSlave = slaveConnections.get(currentSlaveIndex);
				currentSlaveIndex = (currentSlaveIndex + 1) % slaveConnections.size();

			}

			currentSlave.RunStateMachine();

			if (currentSlave.waitingForResponse == false)
				currentSlave = null;
		}
	}

	public void SendLinkLayerTestFunction() {
	}

	public void SetLinkLayerStateChanged(LinkLayerStateChanged callback, Object parameter) {
		stateChanged = callback;
		stateChangedParameter = parameter;
	}

	private void DebugLog(String log) {
		if (debugLog != null) {
			debugLog.accept(log);
		}
	}
}
