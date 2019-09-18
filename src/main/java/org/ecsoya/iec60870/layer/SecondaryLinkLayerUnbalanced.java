/**
 * 
 */
package org.ecsoya.iec60870.layer;

import java.io.IOException;
import java.util.function.Consumer;

import org.ecsoya.iec60870.BufferFrame;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class SecondaryLinkLayerUnbalanced extends SecondaryLinkLayer {

	private boolean expectedFcb = true; // expected value of next frame count bit (FCB)
	private Consumer<String> debugLog;
	private LinkLayer linkLayer;
	private ISecondaryApplicationLayer applicationLayer;

	private int linkLayerAddress = 0;

	public SecondaryLinkLayerUnbalanced(LinkLayer linkLayer, int address, ISecondaryApplicationLayer applicationLayer,
			Consumer<String> debugLog) {
		this.linkLayer = linkLayer;
		this.linkLayerAddress = address;
		this.debugLog = debugLog;
		this.applicationLayer = applicationLayer;
	}

	public int getAddress() {
		return linkLayerAddress;
	}

	public void setAddress(int value) {
		linkLayerAddress = value;
	}

	private boolean CheckFCB(boolean fcb) {
		if (fcb != expectedFcb) {
			System.out.println("ERROR: Frame count bit (FCB) invalid!");
			// TODO change link status
			return false;
		} else {
			expectedFcb = !expectedFcb;
			return true;
		}
	}

	public void HandleMessage(FunctionCodePrimary fcp, boolean isBroadcast, int address, boolean fcb, boolean fcv,
			byte[] msg, int userDataStart, int userDataLength) throws IOException {
		// check frame count bit if fcv == true
		if (fcv) {
			if (CheckFCB(fcb) == false)
				return;
		}

		switch (fcp) {

		case REQUEST_LINK_STATUS:
			DebugLog("SLL - REQUEST LINK STATUS"); {
			boolean accessDemand = applicationLayer.IsClass1DataAvailable();

			linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.STATUS_OF_LINK_OR_ACCESS_DEMAND, linkLayerAddress,
					accessDemand, false);
		}
			break;

		case RESET_REMOTE_LINK:
			DebugLog("SLL - RESET REMOTE LINK"); {
			expectedFcb = true;

			if (linkLayer.linkLayerParameters.isUseSingleCharACK())
				linkLayer.SendSingleCharACK();
			else
				linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);

			applicationLayer.ResetCUReceived(false);
		}

			break;

		case RESET_FCB:
			DebugLog("SLL - RESET FCB"); {
			expectedFcb = true;

			if (linkLayer.linkLayerParameters.isUseSingleCharACK())
				linkLayer.SendSingleCharACK();
			else
				linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);

			applicationLayer.ResetCUReceived(true);
		}
			break;

		case REQUEST_USER_DATA_CLASS_2:
			DebugLog("SLL - REQUEST USER DATA CLASS 2"); {
			BufferFrame asdu = applicationLayer.GetCLass2Data();

			boolean accessDemand = applicationLayer.IsClass1DataAvailable();

			if (asdu != null)
				linkLayer.SendVariableLengthFrameSecondary(FunctionCodeSecondary.RESP_USER_DATA, linkLayerAddress,
						accessDemand, false, asdu);
			else {
				if (linkLayer.linkLayerParameters.isUseSingleCharACK() && (accessDemand == false))
					linkLayer.SendSingleCharACK();
				else
					linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.RESP_NACK_NO_DATA, linkLayerAddress,
							accessDemand, false);
			}

		}
			break;

		case REQUEST_USER_DATA_CLASS_1:
			DebugLog("SLL - REQUEST USER DATA CLASS 1"); {
			BufferFrame asdu = applicationLayer.GetClass1Data();

			boolean accessDemand = applicationLayer.IsClass1DataAvailable();

			if (asdu != null)
				linkLayer.SendVariableLengthFrameSecondary(FunctionCodeSecondary.RESP_USER_DATA, linkLayerAddress,
						accessDemand, false, asdu);
			else {
				if (linkLayer.linkLayerParameters.isUseSingleCharACK() && (accessDemand == false))
					linkLayer.SendSingleCharACK();
				else
					linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.RESP_NACK_NO_DATA, linkLayerAddress,
							accessDemand, false);
			}

		}
			break;

		case USER_DATA_CONFIRMED:
			DebugLog("SLL - USER DATA CONFIRMED");
			if (userDataLength > 0) {
				if (applicationLayer.HandleReceivedData(msg, isBroadcast, userDataStart, userDataLength)) {

					boolean accessDemand = applicationLayer.IsClass1DataAvailable();

					linkLayer.SendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, accessDemand, false);
				}
			}
			break;

		case USER_DATA_NO_REPLY:
			DebugLog("SLL - USER DATA NO REPLY");
			if (userDataLength > 0) {
				applicationLayer.HandleReceivedData(msg, isBroadcast, userDataStart, userDataLength);
			}
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
