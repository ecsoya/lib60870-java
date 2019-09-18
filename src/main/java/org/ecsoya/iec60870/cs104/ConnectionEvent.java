package org.ecsoya.iec60870.cs104;

//====================================================================================================
//The Free Edition of C# to Java Converter limits conversion output to 100 lines per file.

//To subscribe to the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================

/*
 *  Connection.cs
 *
 *  Copyright 2016, 2017 MZ Automation GmbH
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
 * Connection event for CS 104 client (\ref Connection)
 */
public enum ConnectionEvent {
	/**
	 * The connection has been opened
	 */
	OPENED(0),

	/**
	 * The connection has been closed
	 */
	CLOSED(1),

	/**
	 * Conformation of START DT command received (server will send and accept
	 * application layer messages)
	 */
	STARTDT_CON_RECEIVED(2),

	/**
	 * Conformation of STOP DT command received (server will no longer send or
	 * accept application layer messages)
	 */
	STOPDT_CON_RECEIVED(3),

	/**
	 * The connect attempt has failed
	 */
	CONNECT_FAILED(4);

	public static final int SIZE = java.lang.Integer.SIZE;

	private static java.util.HashMap<Integer, ConnectionEvent> mappings;

	public static ConnectionEvent forValue(int value) {
		return getMappings().get(value);
	}

	private static java.util.HashMap<Integer, ConnectionEvent> getMappings() {
		if (mappings == null) {
			synchronized (ConnectionEvent.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, ConnectionEvent>();
				}
			}
		}
		return mappings;
	}

	private int intValue;

	private ConnectionEvent(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}
}