package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class EndOfInitialization extends InformationObject {
	private byte coi;

	public EndOfInitialization(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		coi = msg[startIndex];
	}

	public EndOfInitialization(byte coi) {
		super(0);
		this.coi = coi;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.setNextByte(coi);
	}

	/**
	 * Cause of Initialization (COI)
	 */
	public final byte getCOI() {
		return coi;
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
		return TypeID.M_EI_NA_1;
	}

	public final void setCOI(byte value) {
		coi = value;
	}
}