package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.BinaryCounterReading;

/*
 *  IntegratedTotals.cs
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

public class IntegratedTotals extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 5;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_IT_NA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	private BinaryCounterReading bcr;

	public final BinaryCounterReading getBCR() {
		return bcr;
	}

	public IntegratedTotals(int ioa, BinaryCounterReading bcr) {
		super(ioa);
		this.bcr = bcr;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal IntegratedTotals(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSquence)
	public IntegratedTotals(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSquence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSquence);
		if (!isSquence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		bcr = new BinaryCounterReading(msg, startIndex);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(bcr.GetEncodedValue());
	}
}