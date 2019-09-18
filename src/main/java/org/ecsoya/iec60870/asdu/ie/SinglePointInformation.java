package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

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

public class SinglePointInformation extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 1;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_SP_NA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	private boolean value;

	public final boolean getValue() {
		return this.value;
	}

	public final void setValue(boolean value) {
		this.value = value;
	}

	private QualityDescriptor quality;

	public final QualityDescriptor getQuality() {
		return this.quality;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal SinglePointInformation(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	public SinglePointInformation(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		/* parse SIQ (single point information with qualitiy) */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte siq = msg [startIndex++];
		byte siq = msg[startIndex++];

		value = ((siq & 0x01) == 0x01);

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: quality = new QualityDescriptor((byte)(siq & 0xf0));
		quality = new QualityDescriptor((byte) (siq & 0xf0));
	}

	public SinglePointInformation(int objectAddress, boolean value, QualityDescriptor quality) {
		super(objectAddress);
		this.value = value;
		this.quality = quality;
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte val = quality.EncodedValue;
		byte val = quality.getEncodedValue();

		if (value) {
			val++;
		}

		frame.setNextByte(val);
	}

}