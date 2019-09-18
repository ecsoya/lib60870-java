package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.QualityDescriptor;

/**
 * Single point with CP56Time2a timestamp (M_SP_TB_1)
 */
public class SinglePointWithCP56Time2a extends SinglePointInformation {
	@Override
	public int GetEncodedSize() {
		return 8;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_SP_TB_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	private CP56Time2a timestamp;

	public final CP56Time2a getTimestamp() {
		return this.timestamp;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal SinglePointWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	public SinglePointWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 1; // skip SIQ

		/* parse CP56Time2a (time stamp) */
		timestamp = new CP56Time2a(msg, startIndex);
	}

	public SinglePointWithCP56Time2a(int objectAddress, boolean value, QualityDescriptor quality,
			CP56Time2a timestamp) {
		super(objectAddress, value, quality);
		this.timestamp = timestamp;
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(timestamp.getEncodedValue());
	}
}