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
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class SetpointCommandShortWithCP56Time2a extends SetpointCommandShort {
	private final CP56Time2a timestamp;

	/**
	 * @param parameters
	 * @param msg
	 * @param startIndex
	 * @throws ASDUParsingException
	 */
	public SetpointCommandShortWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex);
		startIndex += parameters.getSizeOfIOA();

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 5; /* skip IOA + float + QOS */

		this.timestamp = new CP56Time2a(msg, startIndex);
	}

	/**
	 * @param objectAddress
	 * @param value
	 * @param qos
	 */
	public SetpointCommandShortWithCP56Time2a(int objectAddress, float value, SetpointCommandQualifier qos,
			CP56Time2a timestamp) {
		super(objectAddress, value, qos);
		this.timestamp = timestamp;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ecsoya.iec60870.asdu.ie.SetpointCommandShort#Encode(org.ecsoya.iec60870.
	 * Frame, org.ecsoya.iec60870.asdu.ApplicationLayerParameters, boolean)
	 */
	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.appendBytes(this.timestamp.getEncodedValue());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ecsoya.iec60870.asdu.ie.SetpointCommandShort#GetEncodedSize()
	 */
	@Override
	public int getEncodedSize() {
		return 12;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ecsoya.iec60870.asdu.ie.SetpointCommandShort#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ecsoya.iec60870.asdu.ie.SetpointCommandShort#getType()
	 */
	@Override
	public TypeID getType() {
		return TypeID.C_SE_TC_1;
	}
}
