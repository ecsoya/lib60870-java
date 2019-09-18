package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.ScaledValue;

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

public class ParameterNormalizedValue extends InformationObject {
	private ScaledValue scaledValue;

	private byte qpm;

	public ParameterNormalizedValue(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		scaledValue = new ScaledValue(msg, startIndex);
		startIndex += 2;

		/* parse QDS (quality) */
		qpm = msg[startIndex++];
	}

	public ParameterNormalizedValue(int objectAddress, float normalizedValue, byte qpm) {
		super(objectAddress);
		scaledValue = new ScaledValue();

		this.setNormalizedValue(normalizedValue);

		this.qpm = qpm;
	}

	public ParameterNormalizedValue(int objectAddress, short rawValue, byte qpm) {
		super(objectAddress);
		scaledValue = new ScaledValue(rawValue);
		this.qpm = qpm;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.appendBytes(scaledValue.getEncodedValue());

		frame.setNextByte(qpm);
	}

	@Override
	public int getEncodedSize() {
		return 3;
	}

	public final float getNormalizedValue() {
		return (float) (scaledValue.getValue() + 0.5) / (float) 32767.5;
	}

	public final byte getQPM() {
		return qpm;
	}

	public final short getRawValue() {
		return scaledValue.getShortValue();
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.P_ME_NA_1;
	}

	public final void setNormalizedValue(float value) {
		/* Check value range */
		if (value > 1.0f) {
			value = 1.0f;
		} else if (value < -1.0f) {
			value = -1.0f;
		}

		this.scaledValue.setValue((int) ((value * 32767.5) - 0.5));
	}

	public final void setRawValue(short value) {
		scaledValue.setShortValue(value);
	}
}