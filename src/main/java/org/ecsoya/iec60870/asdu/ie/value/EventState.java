package org.ecsoya.iec60870.asdu.ie.value;

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

public enum EventState {
	INDETERMINATE_0(0),
	OFF(1),
	ON(2),
	INDETERMINATE_3(3);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, EventState> mappings;

	private static java.util.HashMap<Integer, EventState> getMappings() {
		if (mappings == null) {
			synchronized (EventState.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, EventState>();
				}
			}
		}
		return mappings;
	}

	private EventState(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static EventState forValue(int value) {
		return getMappings().get(value);
	}
}