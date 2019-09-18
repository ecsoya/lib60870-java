/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.DoublePointValue;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class DoublePointWithCP56Time2a extends DoublePointInformation {
	private CP56Time2a timestamp;

	public DoublePointWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence)
			startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize())
			throw new ASDUParsingException("Message too small");

		startIndex += 1; /* skip DIQ */

		/* parse CP56Time2a (time stamp) */
		timestamp = new CP56Time2a(msg, startIndex);
	}

	public DoublePointWithCP56Time2a(int ioa, DoublePointValue value, QualityDescriptor quality, CP56Time2a timestamp) {
		super(ioa, value, quality);
		this.timestamp = timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ecsoya.iec60870.asdu.ie.DoublePointInformation#Encode(org.ecsoya.iec60870
	 * .Frame, org.ecsoya.iec60870.asdu.ApplicationLayerParameters, boolean)
	 */
	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.appendBytes(timestamp.getEncodedValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.DoublePointInformation#GetEncodedSize()
	 */
	@Override
	public int getEncodedSize() {
		return 8;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.DoublePointInformation#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.DoublePointInformation#getType()
	 */
	@Override
	public TypeID getType() {
		return TypeID.M_DP_TB_1;
	}
}
