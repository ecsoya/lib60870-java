package org.ecsoya.iec60870.asdu.ie.value;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.ecsoya.iec60870.ASDUParsingException;

/*
 *  BinaryCounterReading.cs
 *
 *  Copyright 2016 MZ Automation GmbH
 *
 *  This file is part of lib60870.NET
 *
 *  lib60870.NET is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib60870.NET is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with lib60870.NET.  If not, see <http: //www.gnu.org/licenses/>.
 *
 *  See COPYING file for the complete license text.
 */

/**
 * Binary counter reading. Used for tranmission of integrated totals.
 */
public class BinaryCounterReading {

	private byte[] encodedValue = new byte[5];

	public BinaryCounterReading() {
	}

	public BinaryCounterReading(byte[] msg, int startIndex) throws ASDUParsingException {
		if (msg.length < startIndex + 5) {
			throw new ASDUParsingException("Message too small for parsing BinaryCounterReading");
		}

		for (int i = 0; i < 5; i++) {
			encodedValue[i] = msg[startIndex + i];
		}
	}

	/**
	 * Gets or sets the adjusted flag.
	 * 
	 * <value><c>true</c> if adjusted flag is set; otherwise, <c>false</c>.</value>
	 */
	public final boolean getAdjusted() {
		return ((encodedValue[4] & 0x40) == 0x40);
	}

	/**
	 * Gets or sets the carry flag
	 * 
	 * <value><c>true</c> if carry flag set; otherwise, <c>false</c>.</value>
	 */
	public final boolean getCarry() {
		return ((encodedValue[4] & 0x20) == 0x20);
	}

	public final byte[] getEncodedValue() {
		return encodedValue;
	}

	/**
	 * Gets or sets the invalid flag
	 * 
	 * <value><c>true</c> if invalid flag is set; otherwise, <c>false</c>.</value>
	 */
	public final boolean getInvalid() {
		return ((encodedValue[4] & 0x80) == 0x80);
	}

	/**
	 * Gets or sets the sequence number.
	 * 
	 * <value>The sequence number.</value>
	 */
	public final int getSequenceNumber() {
		return (encodedValue[4] & 0x1f);
	}

	/**
	 * Gets or sets the counter value.
	 * 
	 * <value>The value.</value>
	 */
	public final int getValue() {
		int value = encodedValue[0];
		value += (encodedValue[1] * 0x100);
		value += (encodedValue[2] * 0x10000);
		value += (encodedValue[3] * 0x1000000);

		return value;
	}

	public final void setAdjusted(boolean value) {
		if (value) {
			encodedValue[4] |= 0x40;
		} else {
			encodedValue[4] &= 0xbf;
		}
	}

	public final void setCarry(boolean value) {
		if (value) {
			encodedValue[4] |= 0x20;
		} else {
			encodedValue[4] &= 0xdf;
		}
	}

	public final void setInvalid(boolean value) {
		if (value) {
			encodedValue[4] |= 0x80;
		} else {
			encodedValue[4] &= 0x7f;
		}
	}

	public final void setSequenceNumber(int value) {
		int seqNumber = value & 0x1f;
		int flags = encodedValue[4] & 0xe0;

		encodedValue[4] = (byte) (flags | seqNumber);
	}

	public final void setValue(int value) {
		byte[] valueBytes = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(value).array();

		System.arraycopy(valueBytes, 0, encodedValue, 0, 4);
	}
}