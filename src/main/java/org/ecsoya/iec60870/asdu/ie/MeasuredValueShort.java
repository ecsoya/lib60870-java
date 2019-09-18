package org.ecsoya.iec60870.asdu.ie;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

/*
 *  MeasuredValueShortFloat.cs
 *
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

public class MeasuredValueShort extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 5;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_ME_NC_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	private float value;

	public final float getValue() {
		return this.value;
	}

	public final void setValue(float value) {
		this.value = value;
	}

	private QualityDescriptor quality;

	public final QualityDescriptor getQuality() {
		return this.quality;
	}

	public MeasuredValueShort(int objectAddress, float value, QualityDescriptor quality) {
		super(objectAddress);
		this.value = value;
		this.quality = quality;
	}

	public MeasuredValueShort(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		/* parse float value */
		value = ByteBuffer.wrap(msg, startIndex, 4).order(ByteOrder.nativeOrder()).getFloat();
		startIndex += 4;

		/* parse QDS (quality) */
		quality = new QualityDescriptor(msg[startIndex++]);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		byte[] floatEncoded = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putFloat(value).array();

		frame.appendBytes(floatEncoded);

		frame.setNextByte(quality.getEncodedValue());
	}
}