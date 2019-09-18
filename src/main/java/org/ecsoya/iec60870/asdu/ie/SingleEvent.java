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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public SingleEvent(byte encodedValue)
	public SingleEvent(byte encodedValue) {
		this.eventState = EventState.forValue(encodedValue & 0x03);

		this.qdp = new QualityDescriptorP(encodedValue);
	}

	public final EventState getState() {
		return eventState;
	}

	public final void setState(EventState value) {
		eventState = value;
	}

	public final QualityDescriptorP getQDP() {
		return qdp;
	}

	public final void setQDP(QualityDescriptorP value) {
		qdp = value;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte getEncodedValue()
	public final byte getEncodedValue() {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte encodedValue = (byte)((qdp.EncodedValue & 0xfc) + (int) eventState);
		byte encodedValue = (byte) ((qdp.getEncodedValue() & 0xfc) + eventState.getValue());

		return encodedValue;
	}

}