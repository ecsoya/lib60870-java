package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.DoublePointValue;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

public class DoublePointInformation extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 1;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_DP_NA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	private DoublePointValue value = DoublePointValue.values()[0];

	public final DoublePointValue getValue() {
		return this.value;
	}

	private QualityDescriptor quality;

	public final QualityDescriptor getQuality() {
		return this.quality;
	}

	public DoublePointInformation(int ioa, DoublePointValue value, QualityDescriptor quality) {
		super(ioa);
		this.value = value;
		this.quality = quality;
	}

	public DoublePointInformation(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		/* parse DIQ (double point information with qualitiy) */
		byte diq = msg[startIndex++];

		value = DoublePointValue.forValue(diq & 0x03);

		quality = new QualityDescriptor((byte) (diq & 0xf0));
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		byte val = quality.getEncodedValue();

		val += (byte) value.getValue();

		frame.setNextByte(val);
	}
}