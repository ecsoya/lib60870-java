/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class ParameterActivation extends InformationObject {

	public static byte NOT_USED = 0;
	public static byte DE_ACT_PREV_LOADED_PARAMETER = 1;
	public static byte DE_ACT_OBJECT_PARAMETER = 2;
	public static byte DE_ACT_OBJECT_TRANSMISSION = 3;

	/**
	 * Gets the Qualifier of Parameter Activation (QPA)
	 */
	private final byte qpa;

	public ParameterActivation(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize())
			throw new ASDUParsingException("Message too small");

		/* parse QPA */
		qpa = msg[startIndex++];
	}

	public ParameterActivation(int objectAddress, byte qpa) {
		super(objectAddress);
		this.qpa = qpa;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.setNextByte(qpa);
	}

	@Override
	public int getEncodedSize() {
		return 1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.P_AC_NA_1;
	}

}
