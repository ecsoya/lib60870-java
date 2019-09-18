package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP24Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.DoublePointValue;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

public class DoublePointWithCP24Time2a extends DoublePointInformation {
	private CP24Time2a timestamp;

	public DoublePointWithCP24Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 1; // skip DIQ

		/* parse CP24Time2a (time stamp) */
		timestamp = new CP24Time2a(msg, startIndex);
	}

	public DoublePointWithCP24Time2a(int ioa, DoublePointValue value, QualityDescriptor quality, CP24Time2a timestamp) {
		super(ioa, value, quality);
		this.timestamp = timestamp;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.appendBytes(timestamp.getEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 4;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	public final CP24Time2a getTimestamp() {
		return this.timestamp;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_DP_TA_1;
	}
}