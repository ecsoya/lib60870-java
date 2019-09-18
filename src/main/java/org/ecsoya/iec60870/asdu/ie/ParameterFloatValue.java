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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class ParameterFloatValue extends InformationObject {
	private float value;
	private byte qpm;

	public ParameterFloatValue(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		/* parse float value */
		value = ByteBuffer.wrap(msg, startIndex, 4).order(ByteOrder.nativeOrder()).getFloat();
		startIndex += 4;

		/* parse QDS (quality) */
		qpm = msg[startIndex++];
	}

	public ParameterFloatValue(int objectAddress, float value, byte qpm) {
		super(objectAddress);
		this.value = value;
		this.qpm = qpm;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		byte[] floatEncoded = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putFloat(value).array();

		frame.appendBytes(floatEncoded);

		frame.setNextByte(qpm);
	}

	@Override
	public int getEncodedSize() {
		return 5;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.P_ME_NC_1;
	}
}
