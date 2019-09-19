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

	@Override
	public void appendBytes(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			buffer[bufPos++] = bytes[i];
		}
	}

	@Override
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

	@Override
	public byte[] getBuffer() {
		return buffer;
	}

	@Override
	public int getMsgSize() {
		return bufPos;
	}

	@Override
	public void resetFrame() {
		bufPos = startPos;
	}

	@Override
	public void setNextByte(byte value) {
		buffer[bufPos++] = value;
	}
}
