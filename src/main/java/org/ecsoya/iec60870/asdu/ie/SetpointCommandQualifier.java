package org.ecsoya.iec60870.asdu.ie;

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

public class SetpointCommandQualifier {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private byte encodedValue;
	private byte encodedValue;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public SetpointCommandQualifier(byte encodedValue)
	public SetpointCommandQualifier(byte encodedValue) {
		this.encodedValue = encodedValue;
	}

	public SetpointCommandQualifier(boolean select, int ql) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: encodedValue = (byte)(ql & 0x7f);
		encodedValue = (byte) (ql & 0x7f);

		if (select) {
			encodedValue |= 0x80;
		}
	}

	public final int getQL() {
		return (encodedValue & 0x7f);
	}

	public final boolean getSelect() {
		return ((encodedValue & 0x80) == 0x80);
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte GetEncodedValue()
	public final byte GetEncodedValue() {
		return encodedValue;
	}
}