package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class CounterInterrogationCommand extends InformationObject {
	private byte qualifier; // Qualifier of counter interrogation

	public CounterInterrogationCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		qualifier = msg[startIndex++];
	}

	public CounterInterrogationCommand(int ioa, byte qoi) {
		super(ioa);
		this.qualifier = qoi;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.setNextByte(qualifier);
	}

	@Override
	public int getEncodedSize() {
		return 1;
	}

	/**
	 * Gets or sets the QCC (Qualifier of counter interrogation).
	 * 
	 * <value>The QCC</value>
	 */
	public final byte getQualifier() {
		return this.qualifier;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_CI_NA_1;
	}

	public final void setQCC(byte value) {
		qualifier = value;
	}

}