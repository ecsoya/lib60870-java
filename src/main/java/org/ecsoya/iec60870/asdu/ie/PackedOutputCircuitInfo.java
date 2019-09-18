package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP24Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.OutputCircuitInfo;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptorP;

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

public class PackedOutputCircuitInfo extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 7;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_EP_TC_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	private OutputCircuitInfo oci;

	public final OutputCircuitInfo getOCI() {
		return this.oci;
	}

	private QualityDescriptorP qdp;

	public final QualityDescriptorP getQDP() {
		return this.qdp;
	}

	private CP16Time2a operatingTime;

	public final CP16Time2a getOperatingTime() {
		return this.operatingTime;
	}

	private CP24Time2a timestamp;

	public final CP24Time2a getTimestamp() {
		return this.timestamp;
	}

	public PackedOutputCircuitInfo(int objectAddress, OutputCircuitInfo oci, QualityDescriptorP qdp,
			CP16Time2a operatingTime, CP24Time2a timestamp) {
		super(objectAddress);
		this.oci = oci;
		this.qdp = qdp;
		this.operatingTime = operatingTime;
		this.timestamp = timestamp;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal PackedOutputCircuitInfo(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	public PackedOutputCircuitInfo(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		oci = new OutputCircuitInfo(msg[startIndex++]);

		qdp = new QualityDescriptorP(msg[startIndex++]);

		operatingTime = new CP16Time2a(msg, startIndex);
		startIndex += 2;

		/* parse CP56Time2a (time stamp) */
		timestamp = new CP24Time2a(msg, startIndex);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.setNextByte(oci.getEncodedValue());

		frame.setNextByte(qdp.getEncodedValue());

		frame.appendBytes(operatingTime.getEncodedValue());

		frame.appendBytes(timestamp.getEncodedValue());
	}
}