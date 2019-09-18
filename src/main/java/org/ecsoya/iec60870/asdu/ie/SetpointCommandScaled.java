package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.ScaledValue;

public class SetpointCommandScaled extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 3;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_SE_NB_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	private ScaledValue scaledValue;

	public final ScaledValue getScaledValue() {
		return scaledValue;
	}

	private SetpointCommandQualifier qos;

	public final SetpointCommandQualifier getQOS() {
		return qos;
	}

	public SetpointCommandScaled(int objectAddress, ScaledValue value, SetpointCommandQualifier qos) {
		super(objectAddress);
		this.scaledValue = value;
		this.qos = qos;
	}

	public SetpointCommandScaled(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		scaledValue = new ScaledValue(msg, startIndex);
		startIndex += 2;

		this.qos = new SetpointCommandQualifier(msg[startIndex++]);
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.appendBytes(this.scaledValue.GetEncodedValue());

		frame.setNextByte(this.qos.GetEncodedValue());
	}
}
//====================================================================================================
//End of the allowed output for the Free Edition of C# to Java Converter.

//To subscribe to the Premium Edition, visit our website:
//https://www.tangiblesoftwaresolutions.com/order/order-csharp-to-java.html
//====================================================================================================
