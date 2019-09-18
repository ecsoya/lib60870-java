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
 *  MeasuredValueNormalized.cs
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

public class MeasuredValueNormalizedWithoutQuality extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 2;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_ME_ND_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	private ScaledValue scaledValue;

	public final short getRawValue() {
		return scaledValue.getShortValue();
	}

	public final void setRawValue(short value) {
		scaledValue.setShortValue(value);
	}

	public final float getNormalizedValue() {
		return (float) (scaledValue.getValue() + 0.5) / (float) 32767.5;
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

	public MeasuredValueNormalizedWithoutQuality(int objectAddress, float normalizedValue) {
		super(objectAddress);
		this.scaledValue = new ScaledValue();
		this.setNormalizedValue(normalizedValue);
	}

	public MeasuredValueNormalizedWithoutQuality(int objectAddress, short rawValue) {
		super(objectAddress);
		this.scaledValue = new ScaledValue(rawValue);
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal MeasuredValueNormalizedWithoutQuality(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	public MeasuredValueNormalizedWithoutQuality(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		scaledValue = new ScaledValue(msg, startIndex);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(scaledValue.GetEncodedValue());
	}
}