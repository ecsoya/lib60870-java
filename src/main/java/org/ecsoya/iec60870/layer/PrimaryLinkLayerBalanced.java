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
import java.util.function.Consumer;
import java.util.function.Function;

import org.ecsoya.iec60870.BufferFrame;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class PrimaryLinkLayerBalanced extends PrimaryLinkLayer {

	private Consumer<String> debugLog;

	private PrimaryLinkLayerState primaryState = PrimaryLinkLayerState.IDLE;
	private LinkLayerState state = LinkLayerState.IDLE;

	private boolean waitingForResponse = false;
	private long lastSendTime;
	private long originalSendTime;
	private boolean sendLinkLayerTestFunction = false;
	private boolean nextFcb = true;

	private BufferFrame lastSendASDU = null; // last send ASDU for message repetition after timeout

	private int linkLayerAddressOtherStation = 0;

	private LinkLayer linkLayer;

	private Function<Void, BufferFrame> GetUserData;

	private LinkLayerStateChanged stateChangedCallback = null;
	private Object stateChangedCallbackParameter = null;

	public PrimaryLinkLayerBalanced(LinkLayer linkLayer, Function<Void, BufferFrame> getUserData,
			Consumer<String> debugLog) {
		this.debugLog = debugLog;
		this.GetUserData = getUserData;
		this.linkLayer = linkLayer;
	}

	private void debugLog(String log) {
		if (debugLog != null) {
			debugLog.accept(log);
		}
		System.out.println(log);
	}

	/**
	 * @return the linkLayerAddressOtherStation
	 */
	public int getLinkLayerAddressOtherStation() {
		return linkLayerAddressOtherStation;
	}

	public LinkLayerState getLinkLayerState() {
		return state;
	}

	@Override
	public void handleMessage(FunctionCodeSecondary fcs, boolean dir, boolean dfc, int address, byte[] msg,
			int userDataStart, int userDataLength) throws IOException {
		PrimaryLinkLayerState newState = primaryState;

		if (dfc) {

			// TODO stop sending ASDUs; only send Status of link requests

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

			setNewState(LinkLayerState.BUSY);
			primaryState = newState;
			return;
		}

		switch (fcs) {

		case ACK:
			// TODO what to do if we are not waiting for a response?
			debugLog("PLL - received ACK");
			if (primaryState == PrimaryLinkLayerState.EXECUTE_RESET_REMOTE_LINK) {
				newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				setNewState(LinkLayerState.AVAILABLE);
			} else if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM) {

				if (sendLinkLayerTestFunction) {
					sendLinkLayerTestFunction = false;
				}

				newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				setNewState(LinkLayerState.AVAILABLE);
			}

			waitingForResponse = false;
			break;

		case NACK:
			debugLog("PLL - received NACK");
			if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM) {
				newState = PrimaryLinkLayerState.SECONDARY_LINK_LAYER_BUSY;
				setNewState(LinkLayerState.BUSY);
			}
			break;

		case RESP_USER_DATA:

			newState = PrimaryLinkLayerState.IDLE;
			setNewState(LinkLayerState.ERROR);

			break;

		case RESP_NACK_NO_DATA:

			newState = PrimaryLinkLayerState.IDLE;
			setNewState(LinkLayerState.ERROR);

			break;

		case STATUS_OF_LINK_OR_ACCESS_DEMAND:
			debugLog("PLL - received STATUS OF LINK");
			if (primaryState == PrimaryLinkLayerState.EXECUTE_REQUEST_STATUS_OF_LINK) {
				debugLog("PLL - SEND RESET REMOTE LINK to address " + linkLayerAddressOtherStation);
				linkLayer.sendFixedFramePrimary(FunctionCodePrimary.RESET_REMOTE_LINK, linkLayerAddressOtherStation,
						false, false);
				lastSendTime = System.currentTimeMillis();
				waitingForResponse = true;
				newState = PrimaryLinkLayerState.EXECUTE_RESET_REMOTE_LINK;
				setNewState(LinkLayerState.BUSY);
			} else { /* illegal message */
				newState = PrimaryLinkLayerState.IDLE;
				setNewState(LinkLayerState.ERROR);
			}

			break;

		case LINK_SERVICE_NOT_FUNCTIONING:
		case LINK_SERVICE_NOT_IMPLEMENTED:
			debugLog("PLL - link layer service not functioning/not implemented in secondary station");

			if (sendLinkLayerTestFunction) {
				sendLinkLayerTestFunction = false;
			}

			if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM) {
				newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				setNewState(LinkLayerState.AVAILABLE);
			}
			break;

		default:
			debugLog("UNEXPECTED SECONDARY LINK LAYER MESSAGE");
			break;
		}

		debugLog("PLL RECV - old state: " + primaryState + " new state: " + newState);

		primaryState = newState;

	}

	@Override
	public void runStateMachine() throws IOException {
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
					linkLayer.sendFixedFramePrimary(FunctionCodePrimary.REQUEST_LINK_STATUS,
							linkLayerAddressOtherStation, false, false);
					lastSendTime = System.currentTimeMillis();
				}
			} else {
				debugLog("PLL - SEND RESET REMOTE LINK to address " + linkLayerAddressOtherStation);
				linkLayer.sendFixedFramePrimary(FunctionCodePrimary.RESET_REMOTE_LINK, linkLayerAddressOtherStation,
						false, false);
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
					setNewState(LinkLayerState.ERROR);
				}
			} else {
				newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				setNewState(LinkLayerState.AVAILABLE);
			}

			break;

		case LINK_LAYERS_AVAILABLE:

			if (sendLinkLayerTestFunction) {
				debugLog("PLL - SEND TEST LINK");
				linkLayer.sendFixedFramePrimary(FunctionCodePrimary.TEST_FUNCTION_FOR_LINK,
						linkLayerAddressOtherStation, nextFcb, true);
				nextFcb = !nextFcb;
				lastSendTime = System.currentTimeMillis();
				originalSendTime = lastSendTime;
				newState = PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM;
			} else {
				BufferFrame asdu = GetUserData.apply(null);

				if (asdu != null) {

					linkLayer.sendVariableLengthFramePrimary(FunctionCodePrimary.USER_DATA_CONFIRMED,
							linkLayerAddressOtherStation, nextFcb, true, asdu);

					nextFcb = !nextFcb;
					lastSendASDU = asdu;
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
					debugLog("TIMEOUT: ASDU not confirmed after repeated transmission");
					newState = PrimaryLinkLayerState.IDLE;
					setNewState(LinkLayerState.ERROR);
				} else {
					debugLog("TIMEOUT: ASDU not confirmed");

					if (sendLinkLayerTestFunction) {
						debugLog("PLL - REPEAT SEND RESET REMOTE LINK");
						linkLayer.sendFixedFramePrimary(FunctionCodePrimary.TEST_FUNCTION_FOR_LINK,
								linkLayerAddressOtherStation, !nextFcb, true);
					} else {
						debugLog("PLL - repeat last ASDU");
						linkLayer.sendVariableLengthFramePrimary(FunctionCodePrimary.USER_DATA_CONFIRMED,
								linkLayerAddressOtherStation, !nextFcb, true, lastSendASDU);
					}

					lastSendTime = System.currentTimeMillis();
				}
			}

			break;

		case SECONDARY_LINK_LAYER_BUSY:
			// TODO - reject new requests from application layer?
			break;
		default:
			break;

		}

		if (primaryState != newState) {
			debugLog("PLL - old state: " + primaryState + " new state: " + newState);
		}

		primaryState = newState;

	}

	@Override
	public void sendLinkLayerTestFunction() {
		sendLinkLayerTestFunction = true;
	}

	/**
	 * @param linkLayerAddressOtherStation the linkLayerAddressOtherStation to set
	 */
	public void setLinkLayerAddressOtherStation(int linkLayerAddressOtherStation) {
		this.linkLayerAddressOtherStation = linkLayerAddressOtherStation;
	}

	public void setLinkLayerStateChanged(LinkLayerStateChanged handler, Object parameter) {
		stateChangedCallback = handler;
		stateChangedCallbackParameter = parameter;
	}

	private void setNewState(LinkLayerState newState) {
		if (newState != state) {
			state = newState;

			if (stateChangedCallback != null) {
				stateChangedCallback.performStateChanged(stateChangedCallbackParameter, -1, newState);
			}
		}
	}
}
