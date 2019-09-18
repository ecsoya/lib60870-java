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

public enum FileClientState {
	IDLE,
	WAITING_FOR_FILE_READY,
	WAITING_FOR_SECTION_READY, // or for LAST_SECTION
	RECEIVING_SECTION; // waiting for SEGMENT or LAST SEGMENT

	public static final int SIZE = java.lang.Integer.SIZE;

	public static FileClientState forValue(int value) {
		return values()[value];
	}

	public int getValue() {
		return this.ordinal();
	}
}
