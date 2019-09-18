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

import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.layer.PrimaryLinkLayer.LinkLayerBusyException;

interface IPrimaryLinkLayerUnbalanced {

	/// <summary>
	/// Determines whether this channel (slave connecrtion) is ready to transmit a
	/// new application layer message
	/// </summary>
	/// <returns><c>true</c> if this instance is channel available; otherwise,
	/// <c>false</c>.</returns>
	/// <param name="slaveAddress">link layer address of the slave</param>
	boolean isChannelAvailable(int slaveAddress);

	void requestClass1Data(int slaveAddress) throws LinkLayerBusyException;

	void requestClass2Data(int slaveAddress) throws LinkLayerBusyException;

	void resetCU(int slaveAddress);

	void sendConfirmed(int slaveAddress, BufferFrame message) throws LinkLayerBusyException;

	void sendNoReply(int slaveAddress, BufferFrame message) throws LinkLayerBusyException;
}
