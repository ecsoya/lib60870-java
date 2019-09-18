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

		public SlaveConnection(int address, LinkLayer linkLayer, Consumer<String> debugLog,
				PrimaryLinkLayerUnbalanced linkLayerUnbalanced) {
			this.address = address;
			this.linkLayer = linkLayer;
			this.debugLog = debugLog;
			this.linkLayerUnbalanced = linkLayerUnbalanced;
		}

		private void DebugLog(String log) {
			if (debugLog != null) {
				debugLog.accept(log);
			}
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

					if (sendLinkLayerTestFunction) {
						sendLinkLayerTestFunction = false;
					}

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

					linkLayer.sendFixedFramePrimary(FunctionCodePrimary.RESET_REMOTE_LINK, address, false, false);

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
					linkLayerUnbalanced.callbacks.userData(address, msg, userDataStart, userDataLength);

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
				if (linkLayerUnbalanced.callbacks != null) {
					linkLayerUnbalanced.callbacks.accessDemand(address);
				}
			}

			DebugLog("PLL RECV - old state: " + primaryState + " new state: " + newState);

			primaryState = newState;
		}

		public boolean IsMessageWaitingToSend() {
			if (requestClass1Data || requestClass2Data || (nextMessage != null)) {
				return true;
			} else {
				return false;
			}
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
						linkLayer.sendFixedFramePrimary(FunctionCodePrimary.REQUEST_LINK_STATUS, address, false, false);

						lastSendTime = System.currentTimeMillis();
					}

				} else {

					DebugLog("PLL - SEND RESET REMOTE LINK");

					linkLayer.sendFixedFramePrimary(FunctionCodePrimary.RESET_REMOTE_LINK, address, false, false);

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

					linkLayer.sendFixedFramePrimary(FunctionCodePrimary.TEST_FUNCTION_FOR_LINK, address, nextFcb, true);

					nextFcb = !nextFcb;
					lastSendTime = System.currentTimeMillis();
					originalSendTime = lastSendTime;
					waitingForResponse = true;

					newState = PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM;
				} else if (requestClass1Data || requestClass2Data) {

					if (requestClass1Data) {
						DebugLog("PLL - SEND FC 10 - REQ UD 1");

						linkLayer.sendFixedFramePrimary(FunctionCodePrimary.REQUEST_USER_DATA_CLASS_1, address, nextFcb,
								true);
					} else {
						DebugLog("PLL - SEND FC 11 - REQ UD 2");

						linkLayer.sendFixedFramePrimary(FunctionCodePrimary.REQUEST_USER_DATA_CLASS_2, address, nextFcb,
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

						linkLayer.sendVariableLengthFramePrimary(FunctionCodePrimary.USER_DATA_CONFIRMED, address,
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

							linkLayer.sendFixedFramePrimary(FunctionCodePrimary.TEST_FUNCTION_FOR_LINK, address,
									!nextFcb, true);

						} else {

							DebugLog("PLL - SEND FC 03 - USER DATA CONFIRMED [REPEAT]");

							linkLayer.sendVariableLengthFramePrimary(FunctionCodePrimary.USER_DATA_CONFIRMED, address,
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

							linkLayer.sendFixedFramePrimary(FunctionCodePrimary.REQUEST_USER_DATA_CLASS_1, address,
									!nextFcb, true);
						} else if (requestClass2Data) {

							DebugLog("PLL - SEND FC 11 - REQ UD 2 [REPEAT]");

							linkLayer.sendFixedFramePrimary(FunctionCodePrimary.REQUEST_USER_DATA_CLASS_2, address,
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

			if (primaryState != newState) {
				DebugLog("PLL - old state: " + primaryState + " new state: " + newState);
			}

			primaryState = newState;

		}

		private void SetState(LinkLayerState newState) {
			if (linkLayerState != newState) {

				linkLayerState = newState;

				if (linkLayerUnbalanced.stateChanged != null) {
					linkLayerUnbalanced.stateChanged.performStateChanged(linkLayerUnbalanced.stateChangedParameter,
							address, newState);
				}
			}
		}
	}

	private LinkLayer linkLayer;

	// private boolean waitingForResponse = false;

	private Consumer<String> debugLog;

	private List<SlaveConnection> slaveConnections;

	/// <summary>
	/// The current active slave connection.
	/// </summary>
	private SlaveConnection currentSlave = null;

	private BufferFrame nextBroadcastMessage = null;

	private IPrimaryLinkLayerCallbacks callbacks = null;
	private LinkLayerStateChanged stateChanged = null;

	private Object stateChangedParameter = null;

	private int currentSlaveIndex = 0;

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

	public void addSlaveConnection(int slaveAddress) {
		SlaveConnection slave = getSlaveConnection(slaveAddress);

		if (slave == null) {
			slaveConnections.add(new SlaveConnection(slaveAddress, linkLayer, debugLog, this));
		}
	}

	private void debugLog(String log) {
		if (debugLog != null) {
			debugLog.accept(log);
		}
	}

	private SlaveConnection getSlaveConnection(int slaveAddres) {
		for (SlaveConnection connection : slaveConnections) {
			if (connection.address == slaveAddres) {
				return connection;
			}
		}

		return null;
	}

	public LinkLayerState getStateOfSlave(int slaveAddress) throws Exception {
		SlaveConnection connection = getSlaveConnection(slaveAddress);

		if (connection != null) {
			return connection.linkLayerState;
		} else {
			throw new Exception("No slave with this address found");
		}
	}

	@Override
	public void handleMessage(FunctionCodeSecondary fcs, boolean acd, boolean dfc, int address, byte[] msg,
			int userDataStart, int userDataLength) throws Exception {
		SlaveConnection slave = null;

		if (address == -1) {
			slave = currentSlave;
		} else {
			slave = getSlaveConnection(address);
		}

		if (slave != null) {

			slave.HandleMessage(fcs, acd, dfc, address, msg, userDataStart, userDataLength);

		} else {
			debugLog("PLL RECV - response from unknown slave " + address + " !");
		}
	}

	@Override
	public boolean isChannelAvailable(int slaveAddress) {
		SlaveConnection slave = getSlaveConnection(slaveAddress);

		if (slave != null) {
			if (slave.IsMessageWaitingToSend() == false) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void requestClass1Data(int slaveAddress) {
		SlaveConnection slave = getSlaveConnection(slaveAddress);

		if (slave != null) {
			slave.requestClass1Data = true;
			;
		}
	}

	@Override
	public void requestClass2Data(int slaveAddress) throws LinkLayerBusyException {
		SlaveConnection slave = getSlaveConnection(slaveAddress);

		if (slave != null) {
			if (slave.IsMessageWaitingToSend()) {
				throw new LinkLayerBusyException("Message pending");
			} else {
				slave.requestClass2Data = true;
			}
		}
	}

	/********************************
	 * IPrimaryLinkLayerUnbalanced
	 ********************************/

	@Override
	public void resetCU(int slaveAddress) {
		SlaveConnection slave = getSlaveConnection(slaveAddress);

		if (slave != null) {
			slave.resetCu = true;
		}
	}

	@Override
	public void runStateMachine() throws IOException {
		// run all the link layer state machines for the registered slaves

		if (slaveConnections.size() > 0) {

			if (currentSlave == null) {

				/* schedule next slave connection */
				currentSlave = slaveConnections.get(currentSlaveIndex);
				currentSlaveIndex = (currentSlaveIndex + 1) % slaveConnections.size();

			}

			currentSlave.RunStateMachine();

			if (currentSlave.waitingForResponse == false) {
				currentSlave = null;
			}
		}
	}

	@Override
	public void sendConfirmed(int slaveAddress, BufferFrame message) throws LinkLayerBusyException {
		SlaveConnection slave = getSlaveConnection(slaveAddress);

		if (slave != null) {
			if (slave.nextMessage != null) {
				throw new LinkLayerBusyException("Message pending");
			} else {
				slave.nextMessage = message.clone();
				slave.requireConfirmation = true;
			}
		}
	}

	@Override
	public void sendLinkLayerTestFunction() {
	}

	@Override
	public void sendNoReply(int slaveAddress, BufferFrame message) throws LinkLayerBusyException {
		if (slaveAddress == linkLayer.GetBroadcastAddress()) {
			if (nextBroadcastMessage != null) {
				throw new LinkLayerBusyException("Broadcast message pending");
			} else {
				nextBroadcastMessage = message;
			}
		} else {
			SlaveConnection slave = getSlaveConnection(slaveAddress);

			if (slave != null) {
				if (slave.IsMessageWaitingToSend()) {
					throw new LinkLayerBusyException("Message pending");
				} else {
					slave.nextMessage = message;
					slave.requireConfirmation = false;
				}
			}
		}
	}

	public void setLinkLayerStateChanged(LinkLayerStateChanged callback, Object parameter) {
		stateChanged = callback;
		stateChangedParameter = parameter;
	}
}
