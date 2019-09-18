package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class ReadCommand extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 0;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_RD_NA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	public ReadCommand(int ioa) {
		super(ioa);
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal ReadCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
	public ReadCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex) throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
	}
}