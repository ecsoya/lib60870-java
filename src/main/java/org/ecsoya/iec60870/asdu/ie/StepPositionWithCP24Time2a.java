package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP24Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

public class StepPositionWithCP24Time2a extends StepPositionInformation {
	@Override
	public int GetEncodedSize() {
		return 5;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_ST_TA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	private CP24Time2a timestamp;

	public final CP24Time2a getTimestamp() {
		return this.timestamp;
	}

	public final void setTimestamp(CP24Time2a value) {
		this.timestamp = value;
	}

	public StepPositionWithCP24Time2a(int ioa, int value, boolean isTransient, QualityDescriptor quality,
			CP24Time2a timestamp) {
		super(ioa, value, isTransient, quality);
		setTimestamp(timestamp);
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal StepPositionWithCP24Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	public StepPositionWithCP24Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 2; // VTI + quality

		/* parse CP24Time2a (time stamp) */
		timestamp = new CP24Time2a(msg, startIndex);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(timestamp.getEncodedValue());
	}

}