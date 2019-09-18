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
import org.ecsoya.iec60870.asdu.ie.value.StatusAndStatusChangeDetection;

public class PackedSinglePointWithSCD extends InformationObject {

	private StatusAndStatusChangeDetection scd;

	private QualityDescriptor qds;

	public PackedSinglePointWithSCD(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSquence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSquence);

		if (!isSquence) {
			startIndex += parameters.getSizeOfIOA(); /* skip IOA */
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		setScd(new StatusAndStatusChangeDetection(msg, startIndex));
		startIndex += 4;

		setQds(new QualityDescriptor(msg[startIndex++]));
	}

	public PackedSinglePointWithSCD(int objectAddress, StatusAndStatusChangeDetection scd, QualityDescriptor quality) {
		super(objectAddress);
		this.setScd(scd);
		this.setQds(quality);
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.appendBytes(getScd().getEncodedValue());

		frame.setNextByte(getQds().getEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 5;
	}

	/**
	 * @return the qds
	 */
	public QualityDescriptor getQds() {
		return qds;
	}

	/**
	 * @return the scd
	 */
	public StatusAndStatusChangeDetection getScd() {
		return scd;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ecsoya.iec60870.asdu.InformationObject#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_PS_NA_1;
	}

	/**
	 * @param qds the qds to set
	 */
	public void setQds(QualityDescriptor qds) {
		this.qds = qds;
	}

	/**
	 * @param scd the scd to set
	 */
	public void setScd(StatusAndStatusChangeDetection scd) {
		this.scd = scd;
	}
}
