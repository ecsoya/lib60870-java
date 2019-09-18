package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class InterrogationCommand extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 1;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_IC_NA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte qoi;
	private byte qoi;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public byte getQOI()
	public final byte getQOI() {
		return this.qoi;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public void setQOI(byte value)
	public final void setQOI(byte value) {
		qoi = value;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public InterrogationCommand(int ioa, byte qoi)
	public InterrogationCommand(int ioa, byte qoi) {
		super(ioa);
		this.qoi = qoi;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal InterrogationCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
	public InterrogationCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		qoi = msg[startIndex++];
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.setNextByte(qoi);
	}

}