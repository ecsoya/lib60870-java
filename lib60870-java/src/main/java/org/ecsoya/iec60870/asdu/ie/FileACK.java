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
import org.ecsoya.iec60870.asdu.ie.value.AcknowledgeQualifier;
import org.ecsoya.iec60870.asdu.ie.value.FileError;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class FileACK extends InformationObject {
	private final NameOfFile nof;
	private final byte nameOfSection;

	private byte afq;

	public FileACK(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
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

		/* parse AFQ (acknowledge file or section qualifier) */
		afq = msg[startIndex++];
	}

	public FileACK(int objectAddress, NameOfFile nof, byte nameOfSection, AcknowledgeQualifier qualifier,
			FileError errorCode) {
		super(objectAddress);
		this.nof = nof;
		this.nameOfSection = nameOfSection;
		if (qualifier != null) {
			afq = (byte) (afq & 0xf0);
			afq += (byte) qualifier.getValue();
		}
		if (errorCode != null) {
			afq = (byte) (afq & 0x0f);
			afq += (byte) (errorCode.getValue() * 0x10);
		}
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.setNextByte((byte) (nof.getValue() % 256));
		frame.setNextByte((byte) (nof.getValue() / 256));

		frame.setNextByte(nameOfSection);

		frame.setNextByte(afq);
	}

	public AcknowledgeQualifier getAckQualifier() {
		return AcknowledgeQualifier.forValue(afq & 0x0f);
	}

	@Override
	public int getEncodedSize() {
		return 4;
	}

	public FileError getErrorCode() {
		return FileError.forValue(afq / 0x10);
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.F_AF_NA_1;
	}
}
