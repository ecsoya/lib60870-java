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
 * Regulating step command state (RCS) according to IEC 60870-5-101:2003
 * 7.2.6.17
 */
public enum StepCommandValue {
	INVALID_0(0),
	LOWER(1),
	HIGHER(2),
	INVALID_3(3);

	public static final int SIZE = java.lang.Integer.SIZE;

	private static java.util.HashMap<Integer, StepCommandValue> mappings;

	public static StepCommandValue forValue(int value) {
		return getMappings().get(value);
	}

	private static java.util.HashMap<Integer, StepCommandValue> getMappings() {
		if (mappings == null) {
			synchronized (StepCommandValue.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, StepCommandValue>();
				}
			}
		}
		return mappings;
	}

	private int intValue;

	private StepCommandValue(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}
}
