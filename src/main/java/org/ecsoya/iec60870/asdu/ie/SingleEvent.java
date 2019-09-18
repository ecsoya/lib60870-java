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