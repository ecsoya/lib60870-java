package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class InterrogationCommand extends InformationObject {
	private byte qoi;

	public InterrogationCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		qoi = msg[startIndex++];
	}

	public InterrogationCommand(int ioa, byte qoi) {
		super(ioa);
		this.qoi = qoi;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.setNextByte(qoi);
	}

	@Override
	public int getEncodedSize() {
		return 1;
	}

	public final byte getQOI() {
		return this.qoi;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_IC_NA_1;
	}

	public final void setQOI(byte value) {
		qoi = value;
	}

}