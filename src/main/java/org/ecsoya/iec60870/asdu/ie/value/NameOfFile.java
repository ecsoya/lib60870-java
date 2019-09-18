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

/**
 * Name of file (NOF) - describes the type of a file
 */
public enum NameOfFile {
	DEFAULT(0),
	TRANSPARENT_FILE(1),
	DISTURBANCE_DATA(2),
	SEQUENCES_OF_EVENTS(3),
	SEQUENCES_OF_ANALOGUE_VALUES(4);

	private static java.util.HashMap<Integer, NameOfFile> mappings;

	public static NameOfFile forValue(int value) {
		return getMappings().get(value);
	}

	private static java.util.HashMap<Integer, NameOfFile> getMappings() {
		if (mappings == null) {
			synchronized (NameOfFile.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, NameOfFile>();
				}
			}
		}
		return mappings;
	}

	private int shortValue;

	private NameOfFile(int value) {
		shortValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return shortValue;
	}
}
