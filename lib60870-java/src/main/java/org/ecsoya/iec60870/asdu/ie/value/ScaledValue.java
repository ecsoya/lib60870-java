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
package org.ecsoya.iec60870.asdu.ie.value;

import org.ecsoya.iec60870.asdu.ASDUParsingException;

public class ScaledValue {
	private byte[] encodedValue = new byte[2];

	public ScaledValue() {
	}

	public ScaledValue(byte[] msg, int startIndex) throws ASDUParsingException {
		if (msg.length < startIndex + 2) {
			throw new ASDUParsingException("Message too small for parsing ScaledValue");
		}

		for (int i = 0; i < 2; i++) {
			encodedValue[i] = msg[startIndex + i];
		}
	}

	public ScaledValue(int value) {
		this.setValue(value);
	}

	public ScaledValue(short value) {
		this.setShortValue(value);
	}

	public final byte[] getEncodedValue() {
		return encodedValue;
	}

	public final short getShortValue() {
		short uintVal;

		uintVal = encodedValue[0];
		uintVal += (short) (encodedValue[1] * 0x100);

		return uintVal;
	}

	public final int getValue() {
		int value;

		value = encodedValue[0];
		value += (encodedValue[1] * 0x100);

		if (value > 32767) {
			value = value - 65536;
		}

		return value;
	}

	public final void setShortValue(short value) {
		short uintVal = value;

		encodedValue[0] = (byte) (uintVal % 256);
		encodedValue[1] = (byte) (uintVal / 256);
	}

	public final void setValue(int value) {
		if (value > 32767) {
			value = 32767;
		} else if (value < -32768) {
			value = -32768;
		}

		short valueToEncode = (short) value;

		encodedValue[0] = (byte) (valueToEncode % 256);
		encodedValue[1] = (byte) (valueToEncode / 256);
	}

	@Override
	public String toString() {
		return "" + getValue();
	}
}
