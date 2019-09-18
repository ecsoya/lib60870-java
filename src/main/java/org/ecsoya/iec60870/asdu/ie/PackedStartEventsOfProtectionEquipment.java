package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP24Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptorP;
import org.ecsoya.iec60870.asdu.ie.value.StartEvent;

/*
 *  PackedStartEventsOfProtectionEquipment.cs
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

public class PackedStartEventsOfProtectionEquipment extends InformationObject {
	private StartEvent spe;

	private QualityDescriptorP qdp;

	private CP16Time2a elapsedTime;

	private CP24Time2a timestamp;

	public PackedStartEventsOfProtectionEquipment(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		spe = new StartEvent(msg[startIndex++]);
		qdp = new QualityDescriptorP(msg[startIndex++]);

		elapsedTime = new CP16Time2a(msg, startIndex);
		startIndex += 2;

		/* parse CP56Time2a (time stamp) */
		timestamp = new CP24Time2a(msg, startIndex);
	}

	public PackedStartEventsOfProtectionEquipment(int objectAddress, StartEvent spe, QualityDescriptorP qdp,
			CP16Time2a elapsedTime, CP24Time2a timestamp) {
		super(objectAddress);
		this.spe = spe;
		this.qdp = qdp;
		this.elapsedTime = elapsedTime;
		this.timestamp = timestamp;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.setNextByte(spe.getEncodedValue());

		frame.setNextByte(qdp.getEncodedValue());

		frame.appendBytes(elapsedTime.getEncodedValue());

		frame.appendBytes(timestamp.getEncodedValue());
	}

	public final CP16Time2a getElapsedTime() {
		return this.elapsedTime;
	}

	@Override
	public int getEncodedSize() {
		return 7;
	}

	public final QualityDescriptorP getQDP() {
		return qdp;
	}

	public final StartEvent getSPE() {
		return spe;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	public final CP24Time2a getTimestamp() {
		return this.timestamp;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_EP_TB_1;
	}
}