package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class ReadCommand extends InformationObject {
	public ReadCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex) throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
	}

	public ReadCommand(int ioa) {
		super(ioa);
	}

	@Override
	public int getEncodedSize() {
		return 0;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_RD_NA_1;
	}
}