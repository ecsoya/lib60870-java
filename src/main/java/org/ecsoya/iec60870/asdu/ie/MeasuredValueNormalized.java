package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

public class MeasuredValueNormalized extends MeasuredValueNormalizedWithoutQuality {
	@Override
	public int GetEncodedSize() {
		return 3;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_ME_NA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	private QualityDescriptor quality;

	public final QualityDescriptor getQuality() {
		return this.quality;
	}

	public MeasuredValueNormalized(int objectAddress, float value, QualityDescriptor quality) {
		super(objectAddress, value);
		this.quality = quality;
	}

	public MeasuredValueNormalized(int objectAddress, short value, QualityDescriptor quality) {
		super(objectAddress, value);
		this.quality = quality;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal MeasuredValueNormalized(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	public MeasuredValueNormalized(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 2; // normalized value

		/* parse QDS (quality) */
		quality = new QualityDescriptor(msg[startIndex++]);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.setNextByte(quality.getEncodedValue());
	}
}