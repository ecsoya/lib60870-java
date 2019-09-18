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
public class ResetProcessCommand extends InformationObject {

	private final byte qrp;

	public ResetProcessCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < GetEncodedSize())
			throw new ASDUParsingException("Message too small");

		qrp = msg[startIndex++];
	}

	public ResetProcessCommand(int objectAddress, byte qrp) {
		super(objectAddress);
		this.qrp = qrp;
	}

	@Override
	public int GetEncodedSize() {
		return 1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_RP_NA_1;
	}

	public byte getQrp() {
		return qrp;
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);
		frame.setNextByte(qrp);
	}

}
