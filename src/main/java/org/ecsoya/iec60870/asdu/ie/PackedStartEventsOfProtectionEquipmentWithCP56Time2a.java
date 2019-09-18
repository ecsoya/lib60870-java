package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptorP;
import org.ecsoya.iec60870.asdu.ie.value.StartEvent;

public class PackedStartEventsOfProtectionEquipmentWithCP56Time2a extends InformationObject {
	private StartEvent spe;

	private QualityDescriptorP qdp;

	private CP16Time2a elapsedTime;

	private CP56Time2a timestamp;

	public PackedStartEventsOfProtectionEquipmentWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg,
			int startIndex, boolean isSequence) throws ASDUParsingException {
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
		timestamp = new CP56Time2a(msg, startIndex);
	}

	public PackedStartEventsOfProtectionEquipmentWithCP56Time2a(int objectAddress, StartEvent spe,
			QualityDescriptorP qdp, CP16Time2a elapsedTime, CP56Time2a timestamp) {
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
		return 11;
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

	public final CP56Time2a getTimestamp() {
		return this.timestamp;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_EP_TE_1;
	}
}