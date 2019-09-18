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
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

public class Bitstring32 extends InformationObject {
	private int value;

	private QualityDescriptor quality;

	public Bitstring32(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		value = msg[startIndex++];
		value += (msg[startIndex++] * 0x100);
		value += (msg[startIndex++] * 0x10000);
		value += (msg[startIndex++] * 0x1000000);

		quality = new QualityDescriptor(msg[startIndex++]);

	}

	public Bitstring32(int ioa, int value, QualityDescriptor quality) {
		super(ioa);
		this.value = value;
		this.quality = quality;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.setNextByte((byte) (value % 0x100));
		frame.setNextByte((byte) ((value / 0x100) % 0x100));
		frame.setNextByte((byte) ((value / 0x10000) % 0x100));
		frame.setNextByte((byte) (value / 0x1000000));

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
		return TypeID.M_BO_NA_1;
	}

	public final int getValue() {
		return this.value;
	}
}
