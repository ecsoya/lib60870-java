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

public enum FileError {
	DEFAULT(0), // no error
	REQ_MEMORY_NOT_AVAILABLE(1),
	CHECKSUM_FAILED(2),
	UNEXPECTED_COMM_SERVICE(3),
	UNEXPECTED_NAME_OF_FILE(4),
	UNEXPECTED_NAME_OF_SECTION(5);
	private static java.util.HashMap<Integer, FileError> mappings;

	public static FileError forValue(int value) {
		return getMappings().get(value);
	}

	private static java.util.HashMap<Integer, FileError> getMappings() {
		if (mappings == null) {
			synchronized (NameOfFile.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, FileError>();
				}
			}
		}
		return mappings;
	}

	private int shortValue;

	private FileError(int value) {
		shortValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return shortValue;
	}
}
