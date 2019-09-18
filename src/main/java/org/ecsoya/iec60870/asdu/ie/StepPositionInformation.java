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

public class StepPositionInformation extends InformationObject {
	private int value;

	private boolean isTransient;

	private QualityDescriptor quality;

	public StepPositionInformation(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		/* parse VTI (value with transient state indication) */
		byte vti = msg[startIndex++];

		isTransient = ((vti & 0x80) == 0x80);

		value = (vti & 0x7f);

		if (value > 63) {
			value = value - 128;
		}

		quality = new QualityDescriptor(msg[startIndex++]);
	}

	public StepPositionInformation(int ioa, int value, boolean isTransient, QualityDescriptor quality) {
		super(ioa);
		if ((value < -64) || (value > 63)) {
			throw new IndexOutOfBoundsException("value has to be in range -64 .. 63");
		}

		setValue(value);
		setTransient(isTransient);
		this.quality = quality;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		byte vti;

		if (value < 0) {
			vti = (byte) (value + 128);
		} else {
			vti = (byte) value;
		}

		if (isTransient) {
			vti += 0x80;
		}

		frame.setNextByte(vti);

		frame.setNextByte(quality.getEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 2;
	}

	public final QualityDescriptor getQuality() {
		return this.quality;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	/**
	 * Gets a value indicating whether this
	 * <see cref="lib60870.StepPositionInformation"/> is in transient state.
	 * 
	 * <value><c>true</c> if transient; otherwise, <c>false</c>.</value>
	 */
	public final boolean getTransient() {
		return this.isTransient;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_ST_NA_1;
	}

	/**
	 * Step position (range -64 ... +63)
	 * 
	 * <value>The value.</value>
	 */
	public final int getValue() {
		return this.value;
	}

	public final void setTransient(boolean value) {
		this.isTransient = value;
	}

	public final void setValue(int value) {
		if (value > 63) {
			this.value = 63;
		} else if (value < -64) {
			this.value = -64;
		} else {
			this.value = value;
		}
	}

}