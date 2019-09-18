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
package org.ecsoya.iec60870.cs104;

import java.net.SocketAddress;

/**
 * Connection request handler is called when a client tries to connect to the
 * server.
 *
 * @param parameter User provided parameter
 * @param ipAddress IP address of the connecting client
 * @return true if the connection has to be accepted, false otherwise
 */
@FunctionalInterface
public interface ConnectionRequestHandler {
	boolean invoke(Object parameter, SocketAddress ipAddress);
}
