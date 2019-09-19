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
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.ecsoya.iec60870.serial.SerialStream;

/// <summary>
/// Serial transceiver for FT 1.2 type frames
/// </summary>
public class SerialTransceiverFT12 {

	private final SerialStream serialStream;

	private Consumer<String> debugLog;

	// link layer paramters - required to determine address (A) field length in FT
	// 1.2 frame
	private LinkLayerParameters linkLayerParameters;

	// timeout used to wait for the message start character
	private int messageTimeout = 200;

	// timeout to wait for next character in a message
	private int characterTimeout = 200;

	public SerialTransceiverFT12(SerialStream serialStream, LinkLayerParameters linkLayerParameters,
			Consumer<String> debugLog) {
		this.serialStream = serialStream;
		this.debugLog = debugLog;
		this.linkLayerParameters = linkLayerParameters;
	}

	private void debugLog(String log) {
		if (debugLog != null) {
			debugLog.accept(log);
		}
		System.out.println(log);
	}

	public void readNextMessage(byte[] buffer, BiFunction<byte[], Integer, Void> messageHandler) {
		// NOTE: there is some basic decoding required to determine message start/end
		// and synchronization failures.

		try {

			int read = serialStream.read(buffer, 0, 1, messageTimeout);

			if (read == 1) {

				if (buffer[0] == 0x68) {

					int bytesRead = serialStream.read(buffer, 1, 1, characterTimeout);

					if (bytesRead == 1) {

						int msgSize = buffer[1];

						msgSize += 4;

						int readBytes = serialStream.read(buffer, 2, msgSize, characterTimeout);

						if (readBytes == msgSize) {

							msgSize += 2;

							messageHandler.apply(buffer, msgSize);
						} else {
							debugLog("RECV: Timeout reading variable length frame msgSize = " + msgSize
									+ " readBytes = " + readBytes);
						}
					} else {
						debugLog("RECV: SYNC ERROR 1!");
					}
				} else if (buffer[0] == 0x10) {

					int msgSize = 3 + linkLayerParameters.getAddressLength();

					int readBytes = serialStream.read(buffer, 1, msgSize, characterTimeout);

					if (readBytes == msgSize) {

						msgSize += 1;

						messageHandler.apply(buffer, msgSize);
					} else {
						debugLog("RECV: Timeout reading fixed length frame msgSize = " + msgSize + " readBytes = "
								+ readBytes);
					}
				} else if (buffer[0] == 0xe5) {
					int msgSize = 1;

					messageHandler.apply(buffer, msgSize);
				} else {
					debugLog("RECV: SYNC ERROR 2! value = " + buffer[0]);
				}
			}

		} catch (Exception e) {
		}
	}

	/// <summary>
	/// Sends the message over the wire
	/// </summary>
	/// <param name="msg">message data buffer</param>
	/// <param name="msgSize">number of bytes to send</param>
	public void sendMessage(byte[] msg, int msgSize) throws IOException {

		debugLog("SEND " + Arrays.toString(Arrays.copyOf(msg, msgSize)));

		serialStream.write(msg, 0, msgSize);
		serialStream.flush();
	}

	/// <summary>
	/// Sets the timeouts for receiving messages
	/// </summary>
	/// <param name="messageTimeout">timeout to wait for message start (first byte
	/// in the nessage)</param>
	/// <param name="characterTimeout">timeout to wait for next byte (character) in
	/// a message</param>
	public void setTimeouts(int messageTimeout, int characterTimeout) {
		this.messageTimeout = messageTimeout;
		this.characterTimeout = characterTimeout;
	}

	public void connect() throws IOException {
		serialStream.connect();
	}

	public void close() throws IOException {
		serialStream.close();
	}
}
