package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;

public class SetpointCommandNormalizedWithCP56Time2a extends SetpointCommandNormalized {
	private CP56Time2a timestamp;

	public SetpointCommandNormalizedWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 3; // normalized value + qualifier

		this.timestamp = new CP56Time2a(msg, startIndex);
	}

	public SetpointCommandNormalizedWithCP56Time2a(int objectAddress, float value, SetpointCommandQualifier qos,
			CP56Time2a timestamp) {
		super(objectAddress, value, qos);
		this.timestamp = timestamp;
	}

	public SetpointCommandNormalizedWithCP56Time2a(int objectAddress, short value, SetpointCommandQualifier qos,
			CP56Time2a timestamp) {
		super(objectAddress, value, qos);
		this.timestamp = timestamp;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.appendBytes(this.timestamp.getEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 10;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	public final CP56Time2a getTimestamp() {
		return timestamp;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_SE_TA_1;
	}
}