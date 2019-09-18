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
	@Override
	public int GetEncodedSize() {
		return 11;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_EP_TE_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	private StartEvent spe;

	public final StartEvent getSPE() {
		return spe;
	}

	private QualityDescriptorP qdp;

	public final QualityDescriptorP getQDP() {
		return qdp;
	}

	private CP16Time2a elapsedTime;

	public final CP16Time2a getElapsedTime() {
		return this.elapsedTime;
	}

	private CP56Time2a timestamp;

	public final CP56Time2a getTimestamp() {
		return this.timestamp;
	}

	public PackedStartEventsOfProtectionEquipmentWithCP56Time2a(int objectAddress, StartEvent spe,
			QualityDescriptorP qdp, CP16Time2a elapsedTime, CP56Time2a timestamp) {
		super(objectAddress);
		this.spe = spe;
		this.qdp = qdp;
		this.elapsedTime = elapsedTime;
		this.timestamp = timestamp;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal PackedStartEventsOfProtectionEquipmentWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	public PackedStartEventsOfProtectionEquipmentWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg,
			int startIndex, boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		spe = new StartEvent(msg[startIndex++]);
		qdp = new QualityDescriptorP(msg[startIndex++]);

		elapsedTime = new CP16Time2a(msg, startIndex);
		startIndex += 2;

		/* parse CP56Time2a (time stamp) */
		timestamp = new CP56Time2a(msg, startIndex);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.setNextByte(spe.getEncodedValue());

		frame.setNextByte(qdp.getEncodedValue());

		frame.appendBytes(elapsedTime.getEncodedValue());

		frame.appendBytes(timestamp.getEncodedValue());
	}
}