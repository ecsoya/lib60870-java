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
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class FileSegment extends InformationObject {
	private static int ENCODED_SIZE = 4;

	public static int GetMaxDataSize(ApplicationLayerParameters parameters) {
		int maxSize = parameters.getMaxAsduLength() - parameters.getSizeOfTypeId() - parameters.getSizeOfVSQ()
				- parameters.getSizeOfCA() - parameters.getSizeOfCOT() - parameters.getSizeOfIOA() - ENCODED_SIZE;

		return maxSize;
	}

	private NameOfFile nof;

	private byte nameOfSection;

	private byte los; /* length of Segment */

	private byte[] data = null;

	public FileSegment(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); /* skip IOA */
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		int nofValue;

		nofValue = msg[startIndex++];
		nofValue += (msg[startIndex++] * 0x100);

		nof = NameOfFile.forValue(nofValue);

		nameOfSection = msg[startIndex++];

		los = msg[startIndex++];

		if (los > GetMaxDataSize(parameters)) {
			throw new ASDUParsingException("Payload data too large");
		}

		if ((msg.length - startIndex) < los) {
			throw new ASDUParsingException("Message too small");
		}

		data = new byte[los];

		for (int i = 0; i < los; i++) {
			data[i] = msg[startIndex++];
		}
	}

	public FileSegment(int objectAddress, NameOfFile nof, byte nameOfSection, byte[] data) {
		super(objectAddress);
		this.nof = nof;
		this.nameOfSection = nameOfSection;
		this.data = data;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.setNextByte((byte) (nof.getValue() % 256));
		frame.setNextByte((byte) (nof.getValue() / 256));

		frame.setNextByte(nameOfSection);

		frame.setNextByte(los);

		if (data.length > GetMaxDataSize(parameters)) {
			throw new RuntimeException("Payload data too large");
		} else {
			frame.appendBytes(data);
		}
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public int getEncodedSize() {
		return ENCODED_SIZE;
	}

	public byte getLos() {
		return los;
	}

	public byte getNameOfSection() {
		return nameOfSection;
	}

	public NameOfFile getNof() {
		return nof;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.F_SG_NA_1;
	}
}
