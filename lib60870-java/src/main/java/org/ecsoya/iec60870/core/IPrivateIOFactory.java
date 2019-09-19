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
package org.ecsoya.iec60870.core;

import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;

public interface IPrivateIOFactory {
	/**
	 * Decode the information object and create a new InformationObject instance
	 *
	 * @param parameters Application layer parameters required for decoding
	 * @param msg        the received message
	 * @param startIndex start index of the payload in the message
	 * @param isSequence If set to <c>true</c> is sequence.
	 */
	InformationObject decode(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence);

	/**
	 * Gets the encoded payload size of the object (information object size without
	 * the IOA)
	 *
	 * @return The encoded size in bytes
	 */
	int getEncodedSize();
}
