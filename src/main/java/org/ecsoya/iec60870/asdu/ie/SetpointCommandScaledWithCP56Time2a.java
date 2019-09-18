/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.ScaledValue;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class SetpointCommandScaledWithCP56Time2a extends SetpointCommandScaled {

	private final CP56Time2a timestamp;

	/**
	 * @param parameters
	 * @param msg
	 * @param startIndex
	 * @throws ASDUParsingException
	 */
	public SetpointCommandScaledWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize())
			throw new ASDUParsingException("Message too small");

		startIndex += 3; /* scaled value + qualifier */

		this.timestamp = new CP56Time2a(msg, startIndex);
	}

	/**
	 * @param objectAddress
	 * @param value
	 * @param qos
	 */
	public SetpointCommandScaledWithCP56Time2a(int objectAddress, ScaledValue value, SetpointCommandQualifier qos,
			CP56Time2a timestamp) {
		super(objectAddress, value, qos);
		this.timestamp = timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ecsoya.iec60870.asdu.ie.SetpointCommandScaled#Encode(org.ecsoya.iec60870.
	 * Frame, org.ecsoya.iec60870.asdu.ApplicationLayerParameters, boolean)
	 */
	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.appendBytes(this.timestamp.getEncodedValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.SetpointCommandScaled#GetEncodedSize()
	 */
	@Override
	public int getEncodedSize() {
		return 10;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.SetpointCommandScaled#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.SetpointCommandScaled#getType()
	 */
	@Override
	public TypeID getType() {
		return TypeID.C_SE_TB_1;
	}
}
