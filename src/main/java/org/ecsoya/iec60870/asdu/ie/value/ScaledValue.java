package org.ecsoya.iec60870.asdu.ie.value;

import org.ecsoya.iec60870.ASDUParsingException;

/*
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

public class ScaledValue {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private byte[] encodedValue = new byte[2];
	private byte[] encodedValue = new byte[2];

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public ScaledValue(byte[] msg, int startIndex)
	public ScaledValue(byte[] msg, int startIndex) throws ASDUParsingException {
		if (msg.length < startIndex + 2) {
			throw new ASDUParsingException("Message too small for parsing ScaledValue");
		}

		for (int i = 0; i < 2; i++) {
			encodedValue[i] = msg[startIndex + i];
		}
	}

	public ScaledValue() {
	}

	public ScaledValue(int value) {
		this.setValue(value);
	}

	public ScaledValue(short value) {
		this.setShortValue(value);
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte[] GetEncodedValue()
	public final byte[] GetEncodedValue() {
		return encodedValue;
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

	public final void setValue(int value) {
		if (value > 32767) {
			value = 32767;
		} else if (value < -32768) {
			value = -32768;
		}

		short valueToEncode = (short) value;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: encodedValue[0] = (byte)(valueToEncode % 256);
		encodedValue[0] = (byte) (valueToEncode % 256);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: encodedValue[1] = (byte)(valueToEncode / 256);
		encodedValue[1] = (byte) (valueToEncode / 256);
	}

	public final short getShortValue() {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: UInt16 uintVal;
		short uintVal;

		uintVal = encodedValue[0];
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uintVal += (UInt16)(encodedValue [1] * 0x100);
		uintVal += (short) (encodedValue[1] * 0x100);

		return (short) uintVal;
	}

	public final void setShortValue(short value) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: UInt16 uintVal = (UInt16)value;
		short uintVal = (short) value;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: encodedValue[0] = (byte)(uintVal % 256);
		encodedValue[0] = (byte) (uintVal % 256);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: encodedValue[1] = (byte)(uintVal / 256);
		encodedValue[1] = (byte) (uintVal / 256);
	}

	@Override
	public String toString() {
		return "" + getValue();
	}
}