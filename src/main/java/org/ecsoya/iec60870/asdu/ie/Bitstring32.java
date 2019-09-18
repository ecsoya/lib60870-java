package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

/*
 *  BitString32.cs
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

public class Bitstring32 extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 5;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_BO_NA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private UInt32 value;
	private int value;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public UInt32 getValue()
	public final int getValue() {
		return this.value;
	}

	private QualityDescriptor quality;

	public final QualityDescriptor getQuality() {
		return this.quality;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public Bitstring32(int ioa, UInt32 value, QualityDescriptor quality)
	public Bitstring32(int ioa, int value, QualityDescriptor quality) {
		super(ioa);
		this.value = value;
		this.quality = quality;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal Bitstring32(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	public Bitstring32(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		value = msg[startIndex++];
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: value += ((uint)msg [startIndex++] * 0x100);
		value += ((int) msg[startIndex++] * 0x100);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: value += ((uint)msg [startIndex++] * 0x10000);
		value += ((int) msg[startIndex++] * 0x10000);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: value += ((uint)msg [startIndex++] * 0x1000000);
		value += ((int) msg[startIndex++] * 0x1000000);

		quality = new QualityDescriptor(msg[startIndex++]);

	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: frame.SetNextByte((byte)(value % 0x100));
		frame.setNextByte((byte) (value % 0x100));
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: frame.SetNextByte((byte)((value / 0x100) % 0x100));
		frame.setNextByte((byte) ((value / 0x100) % 0x100));
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: frame.SetNextByte((byte)((value / 0x10000) % 0x100));
		frame.setNextByte((byte) ((value / 0x10000) % 0x100));
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: frame.SetNextByte((byte)(value / 0x1000000));
		frame.setNextByte((byte) (value / 0x1000000));

		frame.setNextByte(quality.getEncodedValue());
	}
}