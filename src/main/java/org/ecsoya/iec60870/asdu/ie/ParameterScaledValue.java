package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.ScaledValue;

public class ParameterScaledValue extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 3;
	}

	@Override
	public TypeID getType() {
		return TypeID.P_ME_NB_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	private ScaledValue scaledValue;

	public final ScaledValue getScaledValue() {
		return scaledValue;
	}

	public final void setScaledValue(ScaledValue value) {
		scaledValue = value;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private byte qpm;
	private byte qpm;

	public final float getQPM() {
		return qpm;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public ParameterScaledValue(int objectAddress, ScaledValue value, byte qpm)
	public ParameterScaledValue(int objectAddress, ScaledValue value, byte qpm) {
		super(objectAddress);
		scaledValue = value;

		this.qpm = qpm;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal ParameterScaledValue(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
	public ParameterScaledValue(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		scaledValue = new ScaledValue(msg, startIndex);
		startIndex += 2;

		/* parse QDS (quality) */
		qpm = msg[startIndex++];
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(scaledValue.GetEncodedValue());

		frame.setNextByte(qpm);
	}
}