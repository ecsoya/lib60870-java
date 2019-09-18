package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.DoublePointValue;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

public class DoublePointInformation extends InformationObject {
	private DoublePointValue value = DoublePointValue.values()[0];

	private QualityDescriptor quality;

	public DoublePointInformation(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		/* parse DIQ (double point information with qualitiy) */
		byte diq = msg[startIndex++];

		value = DoublePointValue.forValue(diq & 0x03);

		quality = new QualityDescriptor((byte) (diq & 0xf0));
	}

	public DoublePointInformation(int ioa, DoublePointValue value, QualityDescriptor quality) {
		super(ioa);
		this.value = value;
		this.quality = quality;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		byte val = quality.getEncodedValue();

		val += (byte) value.getValue();

		frame.setNextByte(val);
	}

	@Override
	public int getEncodedSize() {
		return 1;
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
		return TypeID.M_DP_NA_1;
	}

	public final DoublePointValue getValue() {
		return this.value;
	}
}