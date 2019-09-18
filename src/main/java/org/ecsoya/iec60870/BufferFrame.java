package org.ecsoya.iec60870;

/// <summary>
/// Implementation of Frame to encode into a given byte array
/// </summary>
public class BufferFrame implements Frame, Cloneable {

	private byte[] buffer;
	private int startPos;
	private int bufPos;

	public BufferFrame(byte[] buffer, int startPos) {
		this.buffer = buffer;
		this.startPos = startPos;
		this.bufPos = startPos;
	}

	public void appendBytes(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++)
			buffer[bufPos++] = bytes[i];
	}

	public BufferFrame clone() {
		byte[] newBuffer = new byte[getMsgSize()];

		int newBufPos = 0;

		for (int i = startPos; i < bufPos; i++) {
			newBuffer[newBufPos++] = buffer[i];
		}

		BufferFrame clone = new BufferFrame(newBuffer, 0);
		clone.bufPos = newBufPos;

		return clone;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public int getMsgSize() {
		return bufPos;
	}

	public void resetFrame() {
		bufPos = startPos;
	}

	public void setNextByte(byte value) {
		buffer[bufPos++] = value;
	}
}