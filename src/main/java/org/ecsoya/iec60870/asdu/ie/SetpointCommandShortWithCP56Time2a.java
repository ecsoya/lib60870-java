/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class SetpointCommandShortWithCP56Time2a extends SetpointCommandShort {
	private final CP56Time2a timestamp;

	/**
	 * @param parameters
	 * @param msg
	 * @param startIndex
	 * @throws ASDUParsingException
	 */
	public SetpointCommandShortWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex);
		startIndex += parameters.getSizeOfIOA();

		if ((msg.length - startIndex) < GetEncodedSize())
			throw new ASDUParsingException("Message too small");

		startIndex += 5; /* skip IOA + float + QOS */

		this.timestamp = new CP56Time2a(msg, startIndex);
	}

	/**
	 * @param objectAddress
	 * @param value
	 * @param qos
	 */
	public SetpointCommandShortWithCP56Time2a(int objectAddress, float value, SetpointCommandQualifier qos,
			CP56Time2a timestamp) {
		super(objectAddress, value, qos);
		this.timestamp = timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ecsoya.iec60870.asdu.ie.SetpointCommandShort#Encode(org.ecsoya.iec60870.
	 * Frame, org.ecsoya.iec60870.asdu.ApplicationLayerParameters, boolean)
	 */
	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);
		frame.appendBytes(this.timestamp.getEncodedValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.SetpointCommandShort#GetEncodedSize()
	 */
	@Override
	public int GetEncodedSize() {
		return 12;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.SetpointCommandShort#getType()
	 */
	@Override
	public TypeID getType() {
		return TypeID.C_SE_TC_1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.SetpointCommandShort#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return false;
	}
}
