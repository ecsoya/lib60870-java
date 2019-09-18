package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP24Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.BinaryCounterReading;

public class IntegratedTotalsWithCP24Time2a extends IntegratedTotals {
	@Override
	public int GetEncodedSize() {
		return 8;
	}

	@Override
	public TypeID getType() {
		return TypeID.M_IT_TA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return true;
	}

	private CP24Time2a timestamp;

	public final CP24Time2a getTimestamp() {
		return this.timestamp;
	}

	public IntegratedTotalsWithCP24Time2a(int ioa, BinaryCounterReading bcr, CP24Time2a timestamp) {
		super(ioa, bcr);
		this.timestamp = timestamp;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal IntegratedTotalsWithCP24Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	public IntegratedTotalsWithCP24Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence) {
			startIndex += parameters.getSizeOfIOA(); // skip IOA
		}

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		startIndex += 5; // BCR

		timestamp = new CP24Time2a(msg, startIndex);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(timestamp.getEncodedValue());
	}
}