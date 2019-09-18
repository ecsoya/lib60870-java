package org.ecsoya.iec60870.layer;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.ecsoya.iec60870.serial.SerialPort;
import org.ecsoya.iec60870.serial.SerialStream;

/// <summary>
/// Serial transceiver for FT 1.2 type frames
/// </summary>
public class SerialTransceiverFT12 {

	private SerialStream serialStream = null;
	private SerialPort port = null;

	private Consumer<String> debugLog;

	// link layer paramters - required to determine address (A) field length in FT
	// 1.2 frame
	private LinkLayerParameters linkLayerParameters;

	// timeout used to wait for the message start character
	private int messageTimeout = 200;

	// timeout to wait for next character in a message
	private int characterTimeout = 200;

	public SerialTransceiverFT12(SerialPort port, LinkLayerParameters linkLayerParameters, Consumer<String> debugLog) {
		this.port = port;
		this.serialStream = port.getBaseStream();
		this.debugLog = debugLog;
		this.linkLayerParameters = linkLayerParameters;
	}

	public SerialTransceiverFT12(SerialStream serialStream, LinkLayerParameters linkLayerParameters,
			Consumer<String> debugLog) {
//		this.port = null;
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

	public int getBaudRate() {
		if (port != null)
			return port.getBaudRate();
		else
			return 10000000;
	}

	// read the next block of the message
	private int readBytesWithTimeout(byte[] buffer, int startIndex, int count, int timeout) {
		int readByte;
		int readBytes = 0;

		serialStream.setReadTimeout(timeout * count);

		try {

			while ((readByte = serialStream.readByte()) != -1) {
				buffer[startIndex++] = (byte) readByte;

				readBytes++;

				if (readBytes >= count)
					break;
			}
		} catch (IOException e) {
		}

		return readBytes;
	}

	public void readNextMessage(byte[] buffer, BiFunction<byte[], Integer, Void> messageHandler) {
		// NOTE: there is some basic decoding required to determine message start/end
		// and synchronization failures.

		try {

			int read = readBytesWithTimeout(buffer, 0, 1, messageTimeout);

			if (read == 1) {

				if (buffer[0] == 0x68) {

					int bytesRead = readBytesWithTimeout(buffer, 1, 1, characterTimeout);

					if (bytesRead == 1) {

						int msgSize = buffer[1];

						msgSize += 4;

						int readBytes = readBytesWithTimeout(buffer, 2, msgSize, characterTimeout);

						if (readBytes == msgSize) {

							msgSize += 2;

							messageHandler.apply(buffer, msgSize);
						} else
							debugLog("RECV: Timeout reading variable length frame msgSize = " + msgSize
									+ " readBytes = " + readBytes);
					} else {
						debugLog("RECV: SYNC ERROR 1!");
					}
				} else if (buffer[0] == 0x10) {

					int msgSize = 3 + linkLayerParameters.getAddressLength();

					int readBytes = readBytesWithTimeout(buffer, 1, msgSize, characterTimeout);

					if (readBytes == msgSize) {

						msgSize += 1;

						messageHandler.apply(buffer, msgSize);
					} else
						debugLog("RECV: Timeout reading fixed length frame msgSize = " + msgSize + " readBytes = "
								+ readBytes);
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

}