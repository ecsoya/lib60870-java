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

	private boolean checkFCB(boolean fcb) {
		if (fcb != expectedFcb) {
			debugLog("ERROR: Frame count bit (FCB) invalid!");
			// TODO change link status
			return false;
		} else {
			expectedFcb = !expectedFcb;
			return true;
		}
	}

	private void debugLog(String log) {
		if (debugLog != null) {
			debugLog.accept(log);
		}
		System.out.println(log);
	}

	public void handleMessage(FunctionCodePrimary fcp, boolean isBroadcast, int address, boolean fcb, boolean fcv,
			byte[] msg, int userDataStart, int userDataLength) throws IOException {

		if (fcv) {
			if (checkFCB(fcb) == false)
				return;
		}

		switch (fcp) {

		case RESET_REMOTE_LINK:
			expectedFcb = true;
			debugLog("SLL - RECV RESET REMOTE LINK");

			if (linkLayer.linkLayerParameters.isUseSingleCharACK())
				linkLayer.sendSingleCharACK();
			else
				linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);

			break;

		case TEST_FUNCTION_FOR_LINK:
			debugLog("SLL -TEST FUNCTION FOR LINK");
			// TODO check if DCF has to be sent
			if (linkLayer.linkLayerParameters.isUseSingleCharACK())
				linkLayer.sendSingleCharACK();
			else
				linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);
			break;

		case USER_DATA_CONFIRMED:
			debugLog("SLL - USER DATA CONFIRMED");
			if (userDataLength > 0) {

				if (HandleApplicationLayer.handle(address, msg, userDataStart, userDataLength))
					linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);
			}
			break;

		case USER_DATA_NO_REPLY:
			debugLog("SLL - USER DATA NO REPLY");
			if (userDataLength > 0) {
				HandleApplicationLayer.handle(address, msg, userDataStart, userDataLength);
			}
			break;

		case REQUEST_LINK_STATUS:
			debugLog("SLL - RECV REQUEST LINK STATUS");
			sendStatusOfLink(linkLayerAddress);
			break;

		default:
			debugLog("SLL - UNEXPECTED LINK LAYER MESSAGE");
			linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.LINK_SERVICE_NOT_IMPLEMENTED, linkLayerAddress,
					false, false);
			break;

		}
	}

	public void runStateMachine() {

	}

	private void sendStatusOfLink(int address) throws IOException {
		linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.STATUS_OF_LINK_OR_ACCESS_DEMAND, address, false, false);
	}
}
