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

import org.ecsoya.iec60870.asdu.ASDUParsingException;

public class CP16Time2a {
	private byte[] encodedValue = new byte[2];

	public CP16Time2a(byte[] msg, int startIndex) throws ASDUParsingException {
		if (msg.length < startIndex + 3) {
			throw new ASDUParsingException("Message too small for parsing CP16Time2a");
		}

		for (int i = 0; i < 2; i++) {
			encodedValue[i] = msg[startIndex + i];
		}
	}

	public CP16Time2a(int elapsedTimeInMs) {
		encodedValue[0] = (byte) (elapsedTimeInMs % 0x100);
		encodedValue[1] = (byte) (elapsedTimeInMs / 0x100);
	}

	public int getElapsedTimeInMs() {
		return (encodedValue[0] + (encodedValue[1] * 0x100));
	}

	public byte[] getEncodedValue() {
		return encodedValue;
	}

	@Override
	public String toString() {
		return Integer.toString(getElapsedTimeInMs());
	}
}
