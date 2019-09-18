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

public class StatusAndStatusChangeDetection {

	private byte[] encodedValue = new byte[4];

	public StatusAndStatusChangeDetection() {
	}

	public StatusAndStatusChangeDetection(byte[] msg, int startIndex) throws ASDUParsingException {
		if (msg.length < startIndex + 4) {
			throw new ASDUParsingException("Message too small for parsing StatusAndStatusChangeDetection");
		}

		for (int i = 0; i < 4; i++) {
			encodedValue[i] = msg[startIndex + i];
		}
	}

	public final boolean CD(int i) {
		if ((i >= 0) && (i < 16)) {
			return ((int) (getCDn() & (1 << i)) != 0);
		} else {
			return false;
		}
	}

	public final void CD(int i, boolean value) {
		if ((i >= 0) && (i < 16)) {
			if (value) {
				setCDn((short) (getCDn() | (1 << i)));
			} else {
				setCDn((short) (getCDn() & ~(1 << i)));
			}
		}
	}

	public final short getCDn() {
		return (short) (encodedValue[2] + (256 * encodedValue[3]));
	}

	public final byte[] getEncodedValue() {
		return encodedValue;
	}

	public final short getSTn() {
		return (short) (encodedValue[0] + (256 * encodedValue[1]));
	}

	public final void setCDn(short value) {
		encodedValue[2] = (byte) (value % 256);
		encodedValue[3] = (byte) (value / 256);
	}

	public final void setSTn(short value) {
		encodedValue[0] = (byte) (value % 256);
		encodedValue[1] = (byte) (value / 256);
	}

	public final boolean ST(int i) {
		if ((i >= 0) && (i < 16)) {
			return ((int) (getSTn() & (1 << i)) != 0);
		} else {
			return false;
		}
	}

	public final void ST(int i, boolean value) {
		if ((i >= 0) && (i < 16)) {
			if (value) {
				setSTn((short) (getSTn() | (1 << i)));
			} else {
				setSTn((short) (getSTn() & ~(1 << i)));
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(50);

		sb.append("ST:");

		for (int i = 0; i < 16; i++) {
			sb.append(ST(i) ? "1" : "0");
		}

		sb.append(" CD:");

		for (int i = 0; i < 16; i++) {
			sb.append(CD(i) ? "1" : "0");
		}

		return sb.toString();
	}
}