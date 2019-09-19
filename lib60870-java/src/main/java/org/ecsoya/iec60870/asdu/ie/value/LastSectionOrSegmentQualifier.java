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

public enum LastSectionOrSegmentQualifier {
	NOT_USED((byte) 0),
	FILE_TRANSFER_WITHOUT_DEACT((byte) 1),
	FILE_TRANSFER_WITH_DEACT((byte) 2),
	SECTION_TRANSFER_WITHOUT_DEACT((byte) 3),
	SECTION_TRANSFER_WITH_DEACT((byte) 4);

	public static final int SIZE = java.lang.Byte.SIZE;

	private static java.util.HashMap<Byte, LastSectionOrSegmentQualifier> mappings;

	public static LastSectionOrSegmentQualifier forValue(byte value) {
		return getMappings().get(value);
	}

	private static java.util.HashMap<Byte, LastSectionOrSegmentQualifier> getMappings() {
		if (mappings == null) {
			synchronized (LastSectionOrSegmentQualifier.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Byte, LastSectionOrSegmentQualifier>();
				}
			}
		}
		return mappings;
	}

	private byte byteValue;

	private LastSectionOrSegmentQualifier(byte value) {
		byteValue = value;
		getMappings().put(value, this);
	}

	public byte getValue() {
		return byteValue;
	}
}
