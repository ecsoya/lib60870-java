/**
 * 
 */
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

	public void SetLinkLayerStateChanged(LinkLayerStateChanged handler, Object parameter) {
		stateChangedCallback = handler;
		stateChangedCallbackParameter = parameter;
	}

	public LinkLayerState GetLinkLayerState() {
		return state;
	}

	/**
	 * @param linkLayerAddressOtherStation the linkLayerAddressOtherStation to set
	 */
	public void setLinkLayerAddressOtherStation(int linkLayerAddressOtherStation) {
		this.linkLayerAddressOtherStation = linkLayerAddressOtherStation;
	}

	/**
	 * @return the linkLayerAddressOtherStation
	 */
	public int getLinkLayerAddressOtherStation() {
		return linkLayerAddressOtherStation;
	}

	private void SetNewState(LinkLayerState newState) {
		if (newState != state) {
			state = newState;

			if (stateChangedCallback != null)
				stateChangedCallback.performStateChanged(stateChangedCallbackParameter, -1, newState);
		}
	}

	public void HandleMessage(FunctionCodeSecondary fcs, boolean dir, boolean dfc, int address, byte[] msg,
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

			SetNewState(LinkLayerState.BUSY);
			primaryState = newState;
			return;
		}

		switch (fcs) {

		case ACK:
			// TODO what to do if we are not waiting for a response?
			DebugLog("PLL - received ACK");
			if (primaryState == PrimaryLinkLayerState.EXECUTE_RESET_REMOTE_LINK) {
				newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				SetNewState(LinkLayerState.AVAILABLE);
			} else if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM) {

				if (sendLinkLayerTestFunction)
					sendLinkLayerTestFunction = false;

				newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				SetNewState(LinkLayerState.AVAILABLE);
			}

			waitingForResponse = false;
			break;

		case NACK:
			DebugLog("PLL - received NACK");
			if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM) {
				newState = PrimaryLinkLayerState.SECONDARY_LINK_LAYER_BUSY;
				SetNewState(LinkLayerState.BUSY);
			}
			break;

		case RESP_USER_DATA:

			newState = PrimaryLinkLayerState.IDLE;
			SetNewState(LinkLayerState.ERROR);

			break;

		case RESP_NACK_NO_DATA:

			newState = PrimaryLinkLayerState.IDLE;
			SetNewState(LinkLayerState.ERROR);

			break;

		case STATUS_OF_LINK_OR_ACCESS_DEMAND:
			DebugLog("PLL - received STATUS OF LINK");
			if (primaryState == PrimaryLinkLayerState.EXECUTE_REQUEST_STATUS_OF_LINK) {
				DebugLog("PLL - SEND RESET REMOTE LINK to address " + linkLayerAddressOtherStation);
				linkLayer.SendFixedFramePrimary(FunctionCodePrimary.RESET_REMOTE_LINK, linkLayerAddressOtherStation,
						false, false);
				lastSendTime = System.currentTimeMillis();
				waitingForResponse = true;
				newState = PrimaryLinkLayerState.EXECUTE_RESET_REMOTE_LINK;
				SetNewState(LinkLayerState.BUSY);
			} else { /* illegal message */
				newState = PrimaryLinkLayerState.IDLE;
				SetNewState(LinkLayerState.ERROR);
			}

			break;

		case LINK_SERVICE_NOT_FUNCTIONING:
		case LINK_SERVICE_NOT_IMPLEMENTED:
			DebugLog("PLL - link layer service not functioning/not implemented in secondary station");

			if (sendLinkLayerTestFunction)
				sendLinkLayerTestFunction = false;

			if (primaryState == PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM) {
				newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				SetNewState(LinkLayerState.AVAILABLE);
			}
			break;

		default:
			DebugLog("UNEXPECTED SECONDARY LINK LAYER MESSAGE");
			break;
		}

		DebugLog("PLL RECV - old state: " + primaryState + " new state: " + newState);

		primaryState = newState;

	}

	public void SendLinkLayerTestFunction() {
		sendLinkLayerTestFunction = true;
	}

	public void RunStateMachine() {
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
					linkLayer.SendFixedFramePrimary(FunctionCodePrimary.REQUEST_LINK_STATUS,
							linkLayerAddressOtherStation, false, false);
					lastSendTime = System.currentTimeMillis();
				}
			} else {
				DebugLog("PLL - SEND RESET REMOTE LINK to address " + linkLayerAddressOtherStation);
				linkLayer.SendFixedFramePrimary(FunctionCodePrimary.RESET_REMOTE_LINK, linkLayerAddressOtherStation,
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
					SetNewState(LinkLayerState.ERROR);
				}
			} else {
				newState = PrimaryLinkLayerState.LINK_LAYERS_AVAILABLE;
				SetNewState(LinkLayerState.AVAILABLE);
			}

			break;

		case LINK_LAYERS_AVAILABLE:

			if (sendLinkLayerTestFunction) {
				DebugLog("PLL - SEND TEST LINK");
				linkLayer.SendFixedFramePrimary(FunctionCodePrimary.TEST_FUNCTION_FOR_LINK,
						linkLayerAddressOtherStation, nextFcb, true);
				nextFcb = !nextFcb;
				lastSendTime = System.currentTimeMillis();
				originalSendTime = lastSendTime;
				newState = PrimaryLinkLayerState.EXECUTE_SERVICE_SEND_CONFIRM;
			} else {
				BufferFrame asdu = GetUserData.apply(null);

				if (asdu != null) {

					linkLayer.SendVariableLengthFramePrimary(FunctionCodePrimary.USER_DATA_CONFIRMED,
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
					DebugLog("TIMEOUT: ASDU not confirmed after repeated transmission");
					newState = PrimaryLinkLayerState.IDLE;
					SetNewState(LinkLayerState.ERROR);
				} else {
					DebugLog("TIMEOUT: ASDU not confirmed");

					if (sendLinkLayerTestFunction) {
						DebugLog("PLL - REPEAT SEND RESET REMOTE LINK");
						linkLayer.SendFixedFramePrimary(FunctionCodePrimary.TEST_FUNCTION_FOR_LINK,
								linkLayerAddressOtherStation, !nextFcb, true);
					} else {
						DebugLog("PLL - repeat last ASDU");
						linkLayer.SendVariableLengthFramePrimary(FunctionCodePrimary.USER_DATA_CONFIRMED,
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

		if (primaryState != newState)
			DebugLog("PLL - old state: " + primaryState + " new state: " + newState);

		primaryState = newState;

	}

	private void DebugLog(String log) {
		if (debugLog != null) {
			debugLog.accept(log);
		}
		System.out.println(log);
	}
}
