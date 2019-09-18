package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.ScaledValue;

public class ParameterScaledValue extends InformationObject {
	private ScaledValue scaledValue;

	private byte qpm;

	public ParameterScaledValue(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		scaledValue = new ScaledValue(msg, startIndex);
		startIndex += 2;

		/* parse QDS (quality) */
		qpm = msg[startIndex++];
	}

	public ParameterScaledValue(int objectAddress, ScaledValue value, byte qpm) {
		super(objectAddress);
		scaledValue = value;

		this.qpm = qpm;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.appendBytes(scaledValue.getEncodedValue());

		frame.setNextByte(qpm);
	}

	@Override
	public int getEncodedSize() {
		return 3;
	}

	public final float getQPM() {
		return qpm;
	}

	public final ScaledValue getScaledValue() {
		return scaledValue;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.P_ME_NB_1;
	}

	public final void setScaledValue(ScaledValue value) {
		scaledValue = value;
	}
}