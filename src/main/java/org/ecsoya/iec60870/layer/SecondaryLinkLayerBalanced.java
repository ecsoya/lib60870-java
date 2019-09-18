/**
 * 
 */
package org.ecsoya.iec60870.layer;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class SecondaryLinkLayerBalanced extends SecondaryLinkLayer {

	public static interface SecondaryLinkLayerBalancedApplicationLayer {
		boolean handle(int address, byte[] msg, int userDataStart, int userDataLength);
	}

	private boolean expectedFcb = true; // expected value of next frame count bit (FCB)
	private Consumer<String> debugLog;
	private LinkLayer linkLayer;

	private SecondaryLinkLayerBalancedApplicationLayer HandleApplicationLayer;

	private int linkLayerAddress = 0;

	public SecondaryLinkLayerBalanced(LinkLayer linkLayer, int address,
			SecondaryLinkLayerBalancedApplicationLayer handleApplicationLayer, Consumer<String> debugLog) {
		this.linkLayer = linkLayer;
		this.linkLayerAddress = address;
		this.debugLog = debugLog;
		this.HandleApplicationLayer = handleApplicationLayer;
	}

	private void SendStatusOfLink(int address) throws IOException {
		linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.STATUS_OF_LINK_OR_ACCESS_DEMAND, address, false, false);
	}

	private boolean CheckFCB(boolean fcb) {
		if (fcb != expectedFcb) {
			DebugLog("ERROR: Frame count bit (FCB) invalid!");
			// TODO change link status
			return false;
		} else {
			expectedFcb = !expectedFcb;
			return true;
		}
	}

	public void HandleMessage(FunctionCodePrimary fcp, boolean isBroadcast, int address, boolean fcb, boolean fcv,
			byte[] msg, int userDataStart, int userDataLength) throws IOException {

		if (fcv) {
			if (CheckFCB(fcb) == false)
				return;
		}

		switch (fcp) {

		case RESET_REMOTE_LINK:
			expectedFcb = true;
			DebugLog("SLL - RECV RESET REMOTE LINK");

			if (linkLayer.linkLayerParameters.isUseSingleCharACK())
				linkLayer.SendSingleCharACK();
			else
				linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);

			break;

		case TEST_FUNCTION_FOR_LINK:
			DebugLog("SLL -TEST FUNCTION FOR LINK");
			// TODO check if DCF has to be sent
			if (linkLayer.linkLayerParameters.isUseSingleCharACK())
				linkLayer.SendSingleCharACK();
			else
				linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);
			break;

		case USER_DATA_CONFIRMED:
			DebugLog("SLL - USER DATA CONFIRMED");
			if (userDataLength > 0) {

				if (HandleApplicationLayer.handle(address, msg, userDataStart, userDataLength))
					linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);
			}
			break;

		case USER_DATA_NO_REPLY:
			DebugLog("SLL - USER DATA NO REPLY");
			if (userDataLength > 0) {
				HandleApplicationLayer.handle(address, msg, userDataStart, userDataLength);
			}
			break;

		case REQUEST_LINK_STATUS:
			DebugLog("SLL - RECV REQUEST LINK STATUS");
			SendStatusOfLink(linkLayerAddress);
			break;

		default:
			DebugLog("SLL - UNEXPECTED LINK LAYER MESSAGE");
			linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.LINK_SERVICE_NOT_IMPLEMENTED, linkLayerAddress,
					false, false);
			break;

		}
	}

	public void RunStateMachine() {

	}

	private void DebugLog(String log) {
		if (debugLog != null) {
			debugLog.accept(log);
		}
		System.out.println(log);
	}
}
