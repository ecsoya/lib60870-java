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
import org.ecsoya.iec60870.asdu.ie.value.BinaryCounterReading;

/*******************************************************************************
 * lib60870-java is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * You should have received a copy of the GNU General Public License along with
 * lib60870-java. If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/

public class IntegratedTotals extends InformationObject {
	private BinaryCounterReading bcr;

	public IntegratedTotals(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSquence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSquence);
		if (!isSquence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		bcr = new BinaryCounterReading(msg, startIndex);
	}

	public IntegratedTotals(int ioa, BinaryCounterReading bcr) {
		super(ioa);
		this.bcr = bcr;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.appendBytes(bcr.getEncodedValue());
	}

	public final BinaryCounterReading getBCR() {
		return bcr;
	}

	@Override
	public int getEncodedSize() {
		return 5;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_IT_NA_1;
	}
}
