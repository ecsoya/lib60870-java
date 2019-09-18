package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;
import org.ecsoya.iec60870.asdu.ie.value.ScaledValue;

/*
 *  MeasuredValueScaled.cs
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

public class MeasuredValueScaled extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 3;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_ME_NB_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	private ScaledValue scaledValue;

	public final ScaledValue getScaledValue() {
		return this.scaledValue;
	}

	private QualityDescriptor quality;

	public final QualityDescriptor getQuality() {
		return this.quality;
	}

	/**
	 * Initializes a new instance of the <see cref="lib60870.MeasuredValueScaled"/>
	 * class.
	 * 
	 * @param objectAddress Information object address
	 * @param value         scaled value (range -32768 - 32767)
	 * @param quality       quality descriptor (according to IEC 60870-5-101:2003
	 *                      7.2.6.3)
	 */
	public MeasuredValueScaled(int objectAddress, int value, QualityDescriptor quality) {
		super(objectAddress);
		this.scaledValue = new ScaledValue(value);
		this.quality = quality;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal MeasuredValueScaled(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSquence)
	public MeasuredValueScaled(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSquence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSquence);
		if (!isSquence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		scaledValue = new ScaledValue(msg, startIndex);
		startIndex += 2;

		/* parse QDS (quality) */
		quality = new QualityDescriptor(msg[startIndex++]);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(scaledValue.GetEncodedValue());

		frame.setNextByte(quality.getEncodedValue());
	}

}