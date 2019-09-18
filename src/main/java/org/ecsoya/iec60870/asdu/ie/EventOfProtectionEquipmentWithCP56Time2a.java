package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class EventOfProtectionEquipmentWithCP56Time2a extends InformationObject {
	private SingleEvent singleEvent;

	private CP16Time2a elapsedTime;

	private CP56Time2a timestamp;

	public EventOfProtectionEquipmentWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		singleEvent = new SingleEvent(msg[startIndex++]);

		elapsedTime = new CP16Time2a(msg, startIndex);
		startIndex += 2;

		/* parse CP56Time2a (time stamp) */
		timestamp = new CP56Time2a(msg, startIndex);
	}

	public EventOfProtectionEquipmentWithCP56Time2a(int ioa, SingleEvent singleEvent, CP16Time2a elapsedTime,
			CP56Time2a timestamp) {
		super(ioa);
		this.singleEvent = singleEvent;
		this.elapsedTime = elapsedTime;
		this.timestamp = timestamp;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.setNextByte(singleEvent.getEncodedValue());

		frame.appendBytes(elapsedTime.getEncodedValue());

		frame.appendBytes(timestamp.getEncodedValue());
	}

	public final CP16Time2a getElapsedTime() {
		return this.elapsedTime;
	}

	@Override
	public int getEncodedSize() {
		return 10;
	}

	public final SingleEvent getEvent() {
		return singleEvent;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	public final CP56Time2a getTimestamp() {
		return this.timestamp;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_EP_TD_1;
	}
}