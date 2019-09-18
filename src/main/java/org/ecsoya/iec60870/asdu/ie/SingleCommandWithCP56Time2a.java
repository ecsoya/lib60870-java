package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;

public class SingleCommandWithCP56Time2a extends SingleCommand {
	@Override
	public TypeID getType() {
		return TypeID.C_SC_TA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	private CP56Time2a timestamp;

	public final CP56Time2a getTimestamp() {
		return timestamp;
	}

	public SingleCommandWithCP56Time2a(int ioa, boolean command, boolean selectCommand, int qu, CP56Time2a timestamp) {
		super(ioa, command, selectCommand, qu);
		this.timestamp = timestamp;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal SingleCommandWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
	public SingleCommandWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 1; // SCO

		timestamp = new CP56Time2a(msg, startIndex);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(timestamp.getEncodedValue());
	}
}