package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

public class MeasuredValueScaledWithCP56Time2a extends MeasuredValueScaled {
	private CP56Time2a timestamp;

	public MeasuredValueScaledWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 3; // scaledValue + QDS

		/* parse CP56Time2a (time stamp) */
		timestamp = new CP56Time2a(msg, startIndex);
	}

	public MeasuredValueScaledWithCP56Time2a(int objectAddress, int value, QualityDescriptor quality,
			CP56Time2a timestamp) {
		super(objectAddress, value, quality);
		this.timestamp = timestamp;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.appendBytes(timestamp.getEncodedValue());
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
		return this.timestamp;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_ME_TE_1;
	}

}