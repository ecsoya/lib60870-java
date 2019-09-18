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
import java.util.Arrays;
import java.util.function.Consumer;

import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.core.handler.RawMessageHandler;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class LinkLayer {

	// LinkLayer
	protected Consumer<String> debugLog;

	protected byte[] buffer; /* byte buffer to receice and send frames */

	public LinkLayerParameters linkLayerParameters;
	protected SerialTransceiverFT12 transceiver;
	private LinkLayerMode linkLayerMode = LinkLayerMode.BALANCED;

	private PrimaryLinkLayer primaryLinkLayer = null;
	private SecondaryLinkLayer secondaryLinkLayer = null;

	private byte[] SINGLE_CHAR_ACK = new byte[] { (byte) 0xe5 };

	private boolean dir; /* ONLY for balanced link layer */

	private RawMessageHandler receivedRawMessageHandler = null;
	private Object receivedRawMessageHandlerParameter = null;

	private RawMessageHandler sentRawMessageHandler = null;
	private Object sentRawMessageHandlerParameter = null;

	public LinkLayer(byte[] buffer, LinkLayerParameters parameters, SerialTransceiverFT12 transceiver,
			Consumer<String> debugLog) {
		this.buffer = buffer;
		this.linkLayerParameters = parameters;
		this.transceiver = transceiver;
		this.debugLog = debugLog;
	}

	private void debugLog(String log) {
		if (debugLog != null) {
			debugLog.accept(log);
		}
		System.out.println(log);
	}

	int GetBroadcastAddress() {
		if (linkLayerParameters.getAddressLength() == 1) {
			return 255;
		} else if (linkLayerParameters.getAddressLength() == 2) {
			return 65535;
		}

		return 0;
	}

	/**
	 * @return the linkLayerMode
	 */
	public LinkLayerMode getLinkLayerMode() {
		return linkLayerMode;
	}

	public int getOwnAddress() {
		return secondaryLinkLayer.getAddress();
	}

	public long getTimeoutForACK() {
		return linkLayerParameters.getTimeoutForACK();
	}

	public long getTimeoutRepeat() {
		return linkLayerParameters.getTimeoutRepeat();
	}

	void handleMessageAction(byte[] msg, int msgSize) throws Exception {
		debugLog("RECV " + Arrays.toString(msg));

		boolean handleMessage = true;

		if (receivedRawMessageHandler != null) {
			handleMessage = receivedRawMessageHandler.invoke(receivedRawMessageHandlerParameter, msg, msgSize);
		}

		if (handleMessage) {

			if (linkLayerMode == LinkLayerMode.BALANCED) {
				handleMessageBalancedAndPrimaryUnbalanced(buffer, msgSize);
			} else {
				if (secondaryLinkLayer != null) {
					parseHeaderSecondaryUnbalanced(buffer, msgSize);
				} else if (primaryLinkLayer != null) {
					handleMessageBalancedAndPrimaryUnbalanced(buffer, msgSize);
				} else {
					debugLog("ERROR: Neither primary nor secondary link layer available!");
				}
			}
		} else {
			debugLog("Message ignored because of raw message handler");
		}
	}

	public void handleMessageBalancedAndPrimaryUnbalanced(byte[] msg, int msgSize) throws Exception {
		int userDataLength = 0;
		int userDataStart = 0;
		byte c = 0;
		int csStart = 0;
		int csIndex = 0;
		int address = 0; /* address can be ignored in balanced mode? */
		boolean prm = true;
		int fc = 0;

		boolean isAck = false;

		if (msg[0] == 0x68) {

			if (msg[1] != msg[2]) {
				debugLog("ERROR: L fields differ!");
				return;
			}

			userDataLength = msg[1] - linkLayerParameters.getAddressLength() - 1;
			userDataStart = 5 + linkLayerParameters.getAddressLength();

			csStart = 4;
			csIndex = userDataStart + userDataLength;

			// check if message size is reasonable
			if (msgSize != (userDataStart + userDataLength + 2 /* CS + END */)) {
				debugLog("ERROR: Invalid message length");
				return;
			}

			c = msg[4];

			if (linkLayerParameters.getAddressLength() > 0) {
				address += msg[5];
			}

			if (linkLayerParameters.getAddressLength() > 1) {
				address += msg[6] * 0x100;
			}
		} else if (msg[0] == 0x10) {
			c = msg[1];
			csStart = 1;
			csIndex = 2 + linkLayerParameters.getAddressLength();

			if (linkLayerParameters.getAddressLength() > 0) {
				address += msg[2];
			}

			if (linkLayerParameters.getAddressLength() > 1) {
				address += msg[3] * 0x100;
			}

		} else if (msg[0] == 0xe5) {
			isAck = true;
			fc = FunctionCodeSecondary.ACK.getValue();
			prm = false; /* single char ACK is only sent by secondary station */
			debugLog("Received single char ACK");
		} else {
			debugLog("ERROR: Received unexpected message type!");
			return;
		}

		if (isAck == false) {

			// check checksum
			byte checksum = 0;

			for (int i = csStart; i < csIndex; i++) {
				checksum += msg[i];
			}

			if (checksum != msg[csIndex]) {
				debugLog("ERROR: checksum invalid!");
				return;
			}

			// parse C field bits
			fc = c & 0x0f;
			prm = ((c & 0x40) == 0x40);

			if (prm) { /* we are secondary link layer */
				boolean fcb = ((c & 0x20) == 0x20);
				boolean fcv = ((c & 0x10) == 0x10);

				debugLog("PRM=" + (prm == true ? "1" : "0") + " FCB=" + (fcb == true ? "1" : "0") + " FCV="
						+ (fcv == true ? "1" : "0") + " FC=" + fc + "(" + c + ")");

				FunctionCodePrimary fcp = FunctionCodePrimary.get(fc);

				if (secondaryLinkLayer != null) {
					secondaryLinkLayer.handleMessage(fcp, false, address, fcb, fcv, msg, userDataStart, userDataLength);
				} else {
					debugLog("No secondary link layer available!");
				}

			} else { /* we are primary link layer */
				boolean dir = ((c & 0x80) == 0x80); /* DIR - direction for balanced transmission */
				boolean dfc = ((c & 0x10) == 0x10); /* DFC - Data flow control */
				boolean acd = ((c
						& 0x20) == 0x20); /* ACD - access demand for class 1 data - for unbalanced transmission */

				debugLog("PRM=" + (prm == true ? "1" : "0") + " DIR=" + (dir == true ? "1" : "0") + " DFC="
						+ (dfc == true ? "1" : "0") + " FC=" + fc + "(" + c + ")");

				FunctionCodeSecondary fcs = FunctionCodeSecondary.get(fc);

				if (primaryLinkLayer != null) {

					if (linkLayerMode == LinkLayerMode.BALANCED) {
						primaryLinkLayer.handleMessage(fcs, dir, dfc, address, msg, userDataStart, userDataLength);
					} else {
						primaryLinkLayer.handleMessage(fcs, acd, dfc, address, msg, userDataStart, userDataLength);
					}
				} else {
					debugLog("No primary link layer available!");
				}

			}

		} else { /* Single byte ACK */
			if (primaryLinkLayer != null) {
				primaryLinkLayer.handleMessage(FunctionCodeSecondary.ACK, false, false, -1, null, 0, 0);
			}
		}

	}

	/**
	 * @return the dir
	 */
	public boolean isDir() {
		return dir;
	}

	private void parseHeaderSecondaryUnbalanced(byte[] msg, int msgSize) throws IOException {
		int userDataLength = 0;
		int userDataStart = 0;
		byte c;
		int csStart;
		int csIndex;
		int address = 0;

		if (msg[0] == 0x68) {

			if (msg[1] != msg[2]) {
				debugLog("ERROR: L fields differ!");
				return;
			}

			userDataLength = msg[1] - linkLayerParameters.getAddressLength() - 1;
			userDataStart = 5 + linkLayerParameters.getAddressLength();

			csStart = 4;
			csIndex = userDataStart + userDataLength;

			// check if message size is reasonable
			if (msgSize != (userDataStart + userDataLength + 2 /* CS + END */)) {
				debugLog("ERROR: Invalid message length");
				return;
			}

			c = msg[4];
		} else if (msg[0] == 0x10) {
			c = msg[1];
			csStart = 1;
			csIndex = 2 + linkLayerParameters.getAddressLength();

		} else {
			debugLog("ERROR: Received unexpected message type in unbalanced slave mode!");
			return;
		}

		boolean isBroadcast = false;

		// check address
		if (linkLayerParameters.getAddressLength() > 0) {
			address = msg[csStart + 1];

			if (linkLayerParameters.getAddressLength() > 1) {
				address += (msg[csStart + 2] * 0x100);

				if (address == 65535) {
					isBroadcast = true;
				}
			} else {
				if (address == 255) {
					isBroadcast = true;
				}
			}
		}

		int fc = c & 0x0f;
		FunctionCodePrimary fcp = FunctionCodePrimary.get(fc);

		if (isBroadcast) {
			if (fcp != FunctionCodePrimary.USER_DATA_NO_REPLY) {
				debugLog("ERROR: Invalid function code for broadcast message!");
				return;
			}

		} else {
			if (address != secondaryLinkLayer.getAddress()) {
				debugLog("INFO: unknown link layer address -> ignore message");
				return;
			}
		}

		// check checksum
		byte checksum = 0;

		for (int i = csStart; i < csIndex; i++) {
			checksum += msg[i];
		}

		if (checksum != msg[csIndex]) {
			debugLog("ERROR: checksum invalid!");
			return;
		}

		// parse C field bits
		boolean prm = ((c & 0x40) == 0x40);

		if (prm == false) {
			debugLog("ERROR: Received secondary message in unbalanced slave mode!");
			return;
		}

		boolean fcb = ((c & 0x20) == 0x20);
		boolean fcv = ((c & 0x10) == 0x10);

		debugLog("PRM=" + (prm == true ? "1" : "0") + " FCB=" + (fcb == true ? "1" : "0") + " FCV="
				+ (fcv == true ? "1" : "0") + " FC=" + fc + "(" + fcp.toString() + ")");

		if (secondaryLinkLayer != null) {
			secondaryLinkLayer.handleMessage(fcp, isBroadcast, address, fcb, fcv, msg, userDataStart, userDataLength);
		} else {
			debugLog("No secondary link layer available!");
		}
	}

	public void run() {
		transceiver.readNextMessage(buffer, (byte[] msg, Integer msgSize) -> {
			try {
				handleMessageAction(msg, msgSize);
			} catch (IOException e) {
			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		});

		try {
			if (linkLayerMode == LinkLayerMode.BALANCED) {
				primaryLinkLayer.runStateMachine();
				secondaryLinkLayer.runStateMachine();
			} else {
				// TODO avoid redirection by LinkLayer class
				if (primaryLinkLayer != null) {
					primaryLinkLayer.runStateMachine();
				} else if (secondaryLinkLayer != null) {
					secondaryLinkLayer.runStateMachine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendFixedFrame(byte fc, int address, boolean prm, boolean dir, boolean acd, boolean dfc)
			throws IOException {
		int bufPos = 0;

		buffer[bufPos++] = 0x10; /* START */

		byte c = fc;

		if (prm) {
			c += 0x40;
		}

		if (dir) {
			c += 0x80;
		}

		if (acd) {
			c += 0x20;
		}

		if (dfc) {
			c += 0x10;
		}

		buffer[bufPos++] = c;

		if (linkLayerParameters.getAddressLength() > 0) {
			buffer[bufPos++] = (byte) (address % 0x100);

			if (linkLayerParameters.getAddressLength() > 1) {
				buffer[bufPos++] = (byte) ((address / 0x100) % 0x100);
			}
		}

		byte checksum = 0;

		for (int i = 1; i < bufPos; i++) {
			checksum += buffer[i];
		}

		buffer[bufPos++] = checksum;

		buffer[bufPos++] = 0x16; /* END */

		if (sentRawMessageHandler != null) {
			sentRawMessageHandler.invoke(sentRawMessageHandlerParameter, buffer, bufPos);
		}

		transceiver.sendMessage(buffer, bufPos);
	}

	public void sendFixedFramePrimary(FunctionCodePrimary fc, int address, boolean fcb, boolean fcv)
			throws IOException {
		sendFixedFrame((byte) fc.getValue(), address, true, dir, fcb, fcv);
	}

	public void sendFixedFrameSecondary(FunctionCodeSecondary fc, int address, boolean acd, boolean dfc)
			throws IOException {
		sendFixedFrame((byte) fc.getValue(), address, false, dir, acd, dfc);
	}

	public void sendSingleCharACK() throws IOException {
		if (sentRawMessageHandler != null) {
			sentRawMessageHandler.invoke(sentRawMessageHandlerParameter, SINGLE_CHAR_ACK, 1);
		}

		transceiver.sendMessage(SINGLE_CHAR_ACK, 1);
	}

	public void sendTestFunction() {
		if (primaryLinkLayer != null) {
			primaryLinkLayer.sendLinkLayerTestFunction();
		}
	}

	public void sendVariableLengthFramePrimary(FunctionCodePrimary fc, int address, boolean fcb, boolean fcv,
			BufferFrame frame) throws IOException {
		buffer[0] = 0x68; /* START */
		buffer[3] = 0x68; /* START */

		byte c = (byte) fc.getValue();

		if (dir) {
			c += 0x80;
		}

		c += 0x40; // PRM = 1;

		if (fcv) {
			c += 0x10;
		}

		if (fcb) {
			c += 0x20;
		}

		buffer[4] = c;

		int bufPos = 5;

		if (linkLayerParameters.getAddressLength() > 0) {
			buffer[bufPos++] = (byte) (address % 0x100);

			if (linkLayerParameters.getAddressLength() > 1) {
				buffer[bufPos++] = (byte) ((address / 0x100) % 0x100);
			}
		}

		byte[] userData = frame.getBuffer();
		int userDataLength = frame.getMsgSize();

		for (int i = 0; i < userDataLength; i++) {
			buffer[bufPos++] = userData[i];
		}

		int l = 1 + linkLayerParameters.getAddressLength() + frame.getMsgSize();

		if (l > 255) {
			return;
		}

		buffer[1] = (byte) l;
		buffer[2] = (byte) l;

		byte checksum = 0;

		for (int i = 4; i < bufPos; i++) {
			checksum += buffer[i];
		}

		buffer[bufPos++] = checksum;

		buffer[bufPos++] = 0x16; /* END */

		if (sentRawMessageHandler != null) {
			sentRawMessageHandler.invoke(sentRawMessageHandlerParameter, buffer, bufPos);
		}

		transceiver.sendMessage(buffer, bufPos);
	}

	void sendVariableLengthFrameSecondary(FunctionCodeSecondary fc, int address, boolean acd, boolean dfc,
			BufferFrame frame) throws IOException {
		buffer[0] = 0x68; /* START */
		buffer[3] = 0x68; /* START */

		byte c = (byte) (fc.getValue() & 0x1f);

		if (linkLayerMode == LinkLayerMode.BALANCED) {
			if (dir) {
				c += 0x80;
			}
		}

		if (acd) {
			c += 0x20;
		}

		if (dfc) {
			c += 0x10;
		}

		buffer[4] = c;

		int bufPos = 5;

		if (linkLayerParameters.getAddressLength() > 0) {
			buffer[bufPos++] = (byte) (address % 0x100);

			if (linkLayerParameters.getAddressLength() > 1) {
				buffer[bufPos++] = (byte) ((address / 0x100) % 0x100);
			}
		}

		byte[] userData = frame.getBuffer();
		int userDataLength = frame.getMsgSize();

		int l = 1 + linkLayerParameters.getAddressLength() + userDataLength;

		if (l > 255) {
			return;
		}

		buffer[1] = (byte) l;
		buffer[2] = (byte) l;

		for (int i = 0; i < userDataLength; i++) {
			buffer[bufPos++] = userData[i];
		}

		byte checksum = 0;

		for (int i = 4; i < bufPos; i++) {
			checksum += buffer[i];
		}

		buffer[bufPos++] = checksum;

		buffer[bufPos++] = 0x16; /* END */

		if (sentRawMessageHandler != null) {
			sentRawMessageHandler.invoke(sentRawMessageHandlerParameter, buffer, bufPos);
		}

		transceiver.sendMessage(buffer, bufPos);
	}

	/// <summary>
	/// Gets or sets a value indicating whether this balanced <see
	/// cref="lib60870.CS103.LinkLayer"/> has DIR bit set
	/// </summary>
	/// <value><c>true</c> if DI; otherwise, <c>false</c>.</value>
	/**
	 * @param dir the dir to set
	 */
	public void setDir(boolean dir) {
		this.dir = dir;
	}

	/**
	 * @param linkLayerMode the linkLayerMode to set
	 */
	public void setLinkLayerMode(LinkLayerMode linkLayerMode) {
		this.linkLayerMode = linkLayerMode;
	}

	public void setOwnAddress(int value) {
		secondaryLinkLayer.setAddress(value);
	}

	public void SetPrimaryLinkLayer(PrimaryLinkLayer primaryLinkLayer) {
		this.primaryLinkLayer = primaryLinkLayer;
	}

	public void SetReceivedRawMessageHandler(RawMessageHandler handler, Object parameter) {
		receivedRawMessageHandler = handler;
		receivedRawMessageHandlerParameter = parameter;
	}

	public void SetSecondaryLinkLayer(SecondaryLinkLayer secondaryLinkLayer) {
		this.secondaryLinkLayer = secondaryLinkLayer;
	}

	public void SetSentRawMessageHandler(RawMessageHandler handler, Object parameter) {
		sentRawMessageHandler = handler;
		sentRawMessageHandlerParameter = parameter;
	}

	public void setTimeoutForARK(int value) {
		linkLayerParameters.setTimeoutForACK(value);
	}

}
