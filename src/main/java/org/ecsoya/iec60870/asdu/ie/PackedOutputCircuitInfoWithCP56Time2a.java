package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.OutputCircuitInfo;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptorP;

public class PackedOutputCircuitInfoWithCP56Time2a extends InformationObject {
	private OutputCircuitInfo oci;

	private QualityDescriptorP qdp;

	private CP16Time2a operatingTime;

	private CP56Time2a timestamp;

	public PackedOutputCircuitInfoWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		oci = new OutputCircuitInfo(msg[startIndex++]);

		qdp = new QualityDescriptorP(msg[startIndex++]);

		operatingTime = new CP16Time2a(msg, startIndex);
		startIndex += 2;

		/* parse CP56Time2a (time stamp) */
		timestamp = new CP56Time2a(msg, startIndex);
	}

	public PackedOutputCircuitInfoWithCP56Time2a(int objectAddress, OutputCircuitInfo oci, QualityDescriptorP qdp,
			CP16Time2a operatingTime, CP56Time2a timestamp) {
		super(objectAddress);
		this.oci = oci;
		this.qdp = qdp;
		this.operatingTime = operatingTime;
		this.timestamp = timestamp;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.setNextByte(oci.getEncodedValue());

		frame.setNextByte(qdp.getEncodedValue());

		frame.appendBytes(operatingTime.getEncodedValue());

		frame.appendBytes(timestamp.getEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 11;
	}

	public final OutputCircuitInfo getOCI() {
		return this.oci;
	}

	public final CP16Time2a getOperatingTime() {
		return this.operatingTime;
	}

	public final QualityDescriptorP getQDP() {
		return this.qdp;
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
		return TypeID.M_EP_TF_1;
	}
}