/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class MeasuredValueNormalizedWithCP56Time2a extends MeasuredValueNormalized {
	private final CP56Time2a timestamp;

	/**
	 * @param parameters
	 * @param msg
	 * @param startIndex
	 * @param isSequence
	 * @throws ASDUParsingException
	 */
	public MeasuredValueNormalizedWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence)
			startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize())
			throw new ASDUParsingException("Message too small");

		startIndex += 3; /* normalized value + quality */

		/* parse CP56Time2a (time stamp) */
		timestamp = new CP56Time2a(msg, startIndex);
	}

	/**
	 * @param objectAddress
	 * @param value
	 * @param quality
	 */
	public MeasuredValueNormalizedWithCP56Time2a(int objectAddress, float value, QualityDescriptor quality,
			CP56Time2a timestamp) {
		super(objectAddress, value, quality);
		this.timestamp = timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalized#Encode(org.ecsoya.
	 * iec60870.Frame, org.ecsoya.iec60870.asdu.ApplicationLayerParameters, boolean)
	 */
	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.appendBytes(timestamp.getEncodedValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalized#GetEncodedSize()
	 */
	@Override
	public int getEncodedSize() {
		return 10;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalized#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.MeasuredValueNormalized#getType()
	 */
	@Override
	public TypeID getType() {
		return TypeID.M_ME_TD_1;
	}
}
