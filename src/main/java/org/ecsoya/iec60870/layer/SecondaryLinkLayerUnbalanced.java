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

	private boolean checkFCB(boolean fcb) {
		if (fcb != expectedFcb) {
			System.out.println("ERROR: Frame count bit (FCB) invalid!");
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

	@Override
	public int getAddress() {
		return linkLayerAddress;
	}

	@Override
	public void handleMessage(FunctionCodePrimary fcp, boolean isBroadcast, int address, boolean fcb, boolean fcv,
			byte[] msg, int userDataStart, int userDataLength) throws IOException {
		// check frame count bit if fcv == true
		if (fcv) {
			if (checkFCB(fcb) == false) {
				return;
			}
		}

		switch (fcp) {

		case REQUEST_LINK_STATUS:
			debugLog("SLL - REQUEST LINK STATUS"); {
			boolean accessDemand = applicationLayer.isClass1DataAvailable();

			linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.STATUS_OF_LINK_OR_ACCESS_DEMAND, linkLayerAddress,
					accessDemand, false);
		}
			break;

		case RESET_REMOTE_LINK:
			debugLog("SLL - RESET REMOTE LINK"); {
			expectedFcb = true;

			if (linkLayer.linkLayerParameters.isUseSingleCharACK()) {
				linkLayer.sendSingleCharACK();
			} else {
				linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);
			}

			applicationLayer.resetCUReceived(false);
		}

			break;

		case RESET_FCB:
			debugLog("SLL - RESET FCB"); {
			expectedFcb = true;

			if (linkLayer.linkLayerParameters.isUseSingleCharACK()) {
				linkLayer.sendSingleCharACK();
			} else {
				linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, false, false);
			}

			applicationLayer.resetCUReceived(true);
		}
			break;

		case REQUEST_USER_DATA_CLASS_2:
			debugLog("SLL - REQUEST USER DATA CLASS 2"); {
			BufferFrame asdu = applicationLayer.getCLass2Data();

			boolean accessDemand = applicationLayer.isClass1DataAvailable();

			if (asdu != null) {
				linkLayer.sendVariableLengthFrameSecondary(FunctionCodeSecondary.RESP_USER_DATA, linkLayerAddress,
						accessDemand, false, asdu);
			} else {
				if (linkLayer.linkLayerParameters.isUseSingleCharACK() && (accessDemand == false)) {
					linkLayer.sendSingleCharACK();
				} else {
					linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.RESP_NACK_NO_DATA, linkLayerAddress,
							accessDemand, false);
				}
			}

		}
			break;

		case REQUEST_USER_DATA_CLASS_1:
			debugLog("SLL - REQUEST USER DATA CLASS 1"); {
			BufferFrame asdu = applicationLayer.getClass1Data();

			boolean accessDemand = applicationLayer.isClass1DataAvailable();

			if (asdu != null) {
				linkLayer.sendVariableLengthFrameSecondary(FunctionCodeSecondary.RESP_USER_DATA, linkLayerAddress,
						accessDemand, false, asdu);
			} else {
				if (linkLayer.linkLayerParameters.isUseSingleCharACK() && (accessDemand == false)) {
					linkLayer.sendSingleCharACK();
				} else {
					linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.RESP_NACK_NO_DATA, linkLayerAddress,
							accessDemand, false);
				}
			}

		}
			break;

		case USER_DATA_CONFIRMED:
			debugLog("SLL - USER DATA CONFIRMED");
			if (userDataLength > 0) {
				if (applicationLayer.handleReceivedData(msg, isBroadcast, userDataStart, userDataLength)) {

					boolean accessDemand = applicationLayer.isClass1DataAvailable();

					linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.ACK, linkLayerAddress, accessDemand, false);
				}
			}
			break;

		case USER_DATA_NO_REPLY:
			debugLog("SLL - USER DATA NO REPLY");
			if (userDataLength > 0) {
				applicationLayer.handleReceivedData(msg, isBroadcast, userDataStart, userDataLength);
			}
			break;

		default:
			debugLog("SLL - UNEXPECTED LINK LAYER MESSAGE");
			linkLayer.sendFixedFrameSecondary(FunctionCodeSecondary.LINK_SERVICE_NOT_IMPLEMENTED, linkLayerAddress,
					false, false);
			break;
		}
	}

	@Override
	public void runStateMachine() {

	}

	@Override
	public void setAddress(int value) {
		linkLayerAddress = value;
	}
}
