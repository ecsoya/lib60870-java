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

public enum SelectAndCallQualifier {
	DEFAULT((byte) 0),
	SELECT_FILE((byte) 1),
	REQUEST_FILE((byte) 2),
	DEACTIVATE_FILE((byte) 3),
	DELETE_FILE((byte) 4),
	SELECT_SECTION((byte) 5),
	REQUEST_SECTION((byte) 6),
	DEACTIVATE_SECTION((byte) 7);

	public static final int SIZE = java.lang.Byte.SIZE;

	private static java.util.HashMap<Byte, SelectAndCallQualifier> mappings;

	public static SelectAndCallQualifier forValue(byte value) {
		return getMappings().get(value);
	}

	private static java.util.HashMap<Byte, SelectAndCallQualifier> getMappings() {
		if (mappings == null) {
			synchronized (SelectAndCallQualifier.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Byte, SelectAndCallQualifier>();
				}
			}
		}
		return mappings;
	}

	private byte byteValue;

	private SelectAndCallQualifier(byte value) {
		byteValue = value;
		getMappings().put(value, this);
	}

	public byte getValue() {
		return byteValue;
	}
}
