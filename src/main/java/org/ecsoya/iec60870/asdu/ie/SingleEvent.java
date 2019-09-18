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

import org.ecsoya.iec60870.asdu.ie.value.EventState;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptorP;

public class SingleEvent {
	private QualityDescriptorP qdp;

	private EventState eventState = EventState.values()[0];

	public SingleEvent() {
		this.eventState = EventState.INDETERMINATE_0;
		this.qdp = new QualityDescriptorP();
	}

	public SingleEvent(byte encodedValue) {
		this.eventState = EventState.forValue(encodedValue & 0x03);

		this.qdp = new QualityDescriptorP(encodedValue);
	}

	public final byte getEncodedValue() {
		byte encodedValue = (byte) ((qdp.getEncodedValue() & 0xfc) + eventState.getValue());

		return encodedValue;
	}

	public final QualityDescriptorP getQDP() {
		return qdp;
	}

	public final EventState getState() {
		return eventState;
	}

	public final void setQDP(QualityDescriptorP value) {
		qdp = value;
	}

	public final void setState(EventState value) {
		eventState = value;
	}

}
