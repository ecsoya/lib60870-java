package org.ecsoya.iec60870.asdu.ie.value;

//====================================================================================================
//The Free Edition of C# to Java Converter limits conversion output to 100 lines per file.

//To subscribe to the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================

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

	private int intValue;
	private static java.util.HashMap<Integer, StepCommandValue> mappings;

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

	private StepCommandValue(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static StepCommandValue forValue(int value) {
		return getMappings().get(value);
	}
}