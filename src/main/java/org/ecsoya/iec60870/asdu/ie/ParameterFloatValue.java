/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class ParameterFloatValue extends InformationObject {
	private float value;
	private byte qpm;

	public ParameterFloatValue(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < GetEncodedSize())
			throw new ASDUParsingException("Message too small");

		/* parse float value */
		value = ByteBuffer.wrap(msg, startIndex, 4).order(ByteOrder.nativeOrder()).getFloat();
		startIndex += 4;

		/* parse QDS (quality) */
		qpm = msg[startIndex++];
	}

	public ParameterFloatValue(int objectAddress, float value, byte qpm) {
		super(objectAddress);
		this.value = value;
		this.qpm = qpm;
	}

	@Override
	public int GetEncodedSize() {
		return 5;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.P_ME_NC_1;
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);
		byte[] floatEncoded = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putFloat(value).array();

		frame.appendBytes(floatEncoded);

		frame.setNextByte(qpm);
	}
}
