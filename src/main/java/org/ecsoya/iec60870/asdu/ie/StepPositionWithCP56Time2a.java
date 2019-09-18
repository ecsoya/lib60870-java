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
public class StepPositionWithCP56Time2a extends StepPositionInformation {
	private CP56Time2a timestamp;

	/**
	 * @param ioa
	 * @param value
	 * @param isTransient
	 * @param quality
	 */
	public StepPositionWithCP56Time2a(int ioa, int value, boolean isTransient, QualityDescriptor quality,
			CP56Time2a timestamp) {
		super(ioa, value, isTransient, quality);
		this.timestamp = timestamp;
	}

	/**
	 * @param parameters
	 * @param msg
	 * @param startIndex
	 * @param isSequence
	 * @throws ASDUParsingException
	 */
	public StepPositionWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence)
			startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < GetEncodedSize())
			throw new ASDUParsingException("Message too small");

		startIndex += 2; /* skip VTI + quality */

		/* parse CP24Time2a (time stamp) */
		timestamp = new CP56Time2a(msg, startIndex);
	}

	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(timestamp.getEncodedValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.StepPositionInformation#GetEncodedSize()
	 */
	@Override
	public int GetEncodedSize() {
		return 9;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.StepPositionInformation#getType()
	 */
	@Override
	public TypeID getType() {
		return TypeID.M_ST_TB_1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ecsoya.iec60870.asdu.ie.StepPositionInformation#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return false;
	}
}
