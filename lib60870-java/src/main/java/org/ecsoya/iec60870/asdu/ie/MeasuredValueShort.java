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
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

/*******************************************************************************
 * lib60870-java is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * You should have received a copy of the GNU General Public License along with
 * lib60870-java. If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/

public class MeasuredValueShort extends InformationObject {
	private float value;

	private QualityDescriptor quality;

	public MeasuredValueShort(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		/* parse float value */
		value = ByteBuffer.wrap(msg, startIndex, 4).order(ByteOrder.nativeOrder()).getFloat();
		startIndex += 4;

		/* parse QDS (quality) */
		quality = new QualityDescriptor(msg[startIndex++]);
	}

	public MeasuredValueShort(int objectAddress, float value, QualityDescriptor quality) {
		super(objectAddress);
		this.value = value;
		this.quality = quality;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		byte[] floatEncoded = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putFloat(value).array();

		frame.appendBytes(floatEncoded);

		frame.setNextByte(quality.getEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 5;
	}

	public final QualityDescriptor getQuality() {
		return this.quality;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_ME_NC_1;
	}

	public final float getValue() {
		return this.value;
	}

	public final void setValue(float value) {
		this.value = value;
	}
}
