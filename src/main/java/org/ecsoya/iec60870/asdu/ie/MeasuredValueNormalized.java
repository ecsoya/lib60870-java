package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

public class MeasuredValueNormalized extends MeasuredValueNormalizedWithoutQuality {
	private QualityDescriptor quality;

	public MeasuredValueNormalized(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 2; // normalized value

		/* parse QDS (quality) */
		quality = new QualityDescriptor(msg[startIndex++]);
	}

	public MeasuredValueNormalized(int objectAddress, float value, QualityDescriptor quality) {
		super(objectAddress, value);
		this.quality = quality;
	}

	public MeasuredValueNormalized(int objectAddress, short value, QualityDescriptor quality) {
		super(objectAddress, value);
		this.quality = quality;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.setNextByte(quality.getEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 3;
	}

	public final QualityDescriptor getQuality() {
		return this.quality;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_ME_NA_1;
	}
}