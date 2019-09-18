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

public class ParameterScaledValue extends InformationObject {
	private ScaledValue scaledValue;

	private byte qpm;

	public ParameterScaledValue(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
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

	public ParameterScaledValue(int objectAddress, ScaledValue value, byte qpm) {
		super(objectAddress);
		scaledValue = value;

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

	public final float getQPM() {
		return qpm;
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
		return TypeID.P_ME_NB_1;
	}

	public final void setScaledValue(ScaledValue value) {
		scaledValue = value;
	}
}
