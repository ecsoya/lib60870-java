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
package org.ecsoya.iec60870.layer;

import java.io.IOException;

import org.ecsoya.iec60870.core.ConnectionException;

public interface IPrimaryLinkLayerCallbacks {

	/// <summary>
	/// Indicate an access demand request form the client (ACD bit set in response)
	/// </summary>
	/// <param name="slaveAddress">address of the slave that requested the access
	/// demand</param>
	void handleAccessDemand(int slaveAddress);

	/// <summary>
	/// A former request to the slave (UD Class 1, UD Class 2, confirmed...)
	/// resulted in a timeout
	/// Station does not respond indication
	/// </summary>
	/// <param name="slaveAddress">address of the slave that caused the
	/// timeout</param>
	void handleTimeout(int slaveAddress);

	/// <summary>
	/// User data (application layer data) received from a slave
	/// </summary>
	/// <param name="slaveAddress">address of the slave that sent the data</param>
	/// <param name="message">buffer containing the received message</param>
	/// <param name="start">start of user data in the buffer</param>
	/// <param name="length">length of user data in the buffer</param>
	void handleUserData(int slaveAddress, byte[] message, int start, int length) throws ConnectionException, IOException;
}
