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

import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.ScaledValue;

public class SetpointCommandScaled extends InformationObject {
	private ScaledValue scaledValue;

	private SetpointCommandQualifier qos;

	public SetpointCommandScaled(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		scaledValue = new ScaledValue(msg, startIndex);
		startIndex += 2;

		this.qos = new SetpointCommandQualifier(msg[startIndex++]);
	}

	public SetpointCommandScaled(int objectAddress, ScaledValue value, SetpointCommandQualifier qos) {
		super(objectAddress);
		this.scaledValue = value;
		this.qos = qos;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.appendBytes(this.scaledValue.getEncodedValue());

		frame.setNextByte(this.qos.GetEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 3;
	}

	public final SetpointCommandQualifier getQOS() {
		return qos;
	}

	public final ScaledValue getScaledValue() {
		return scaledValue;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_SE_NB_1;
	}
}
//====================================================================================================
//End of the allowed output for the Free Edition of C# to Java Converter.

//To subscribe to the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================
