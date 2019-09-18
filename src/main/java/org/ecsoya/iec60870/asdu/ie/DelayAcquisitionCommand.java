/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class DelayAcquisitionCommand extends InformationObject {

	private final CP16Time2a delay;

	public DelayAcquisitionCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < GetEncodedSize())
			throw new ASDUParsingException("Message too small");

		/* parse CP16Time2a (time stamp) */
		delay = new CP16Time2a(msg, startIndex);
	}

	public DelayAcquisitionCommand(int objectAddress, CP16Time2a delay) {
		super(objectAddress);
		this.delay = delay;
	}

	@Override
	public int GetEncodedSize() {
		return 2;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_CD_NA_1;
	}

	public CP16Time2a getDelay() {
		return delay;
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);
		frame.appendBytes(delay.getEncodedValue());
	}

}
