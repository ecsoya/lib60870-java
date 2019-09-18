/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class SectionReady extends InformationObject {
	private NameOfFile nof;
	private byte nameOfSection;

	private int lengthOfSection;

	private byte srq;// SRQ (section ready qualifier)

	public SectionReady(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence)
			startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < GetEncodedSize())
			throw new ASDUParsingException("Message too small");

		int nofValue;

		nofValue = msg[startIndex++];
		nofValue += (msg[startIndex++] * 0x100);

		nof = NameOfFile.forValue(nofValue);

		nameOfSection = msg[startIndex++];

		lengthOfSection = msg[startIndex++];
		lengthOfSection += (msg[startIndex++] * 0x100);
		lengthOfSection += (msg[startIndex++] * 0x10000);

		/* parse SRQ (section read qualifier) */
		srq = msg[startIndex++];
	}

	public SectionReady(int objectAddress, NameOfFile nof, byte nameOfSection, int lengthOfSection, boolean notReady) {
		super(objectAddress);
		this.nof = nof;
		this.nameOfSection = nameOfSection;
		this.lengthOfSection = lengthOfSection;

		if (notReady)
			srq = (byte) 0x80;
		else
			srq = 0;
	}

	public boolean isNotReady() {
		return ((srq & 0x80) == 0x80);
	}

	@Override
	public int GetEncodedSize() {
		return 7;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.F_SR_NA_1;
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);
		frame.setNextByte((byte) (nof.getValue() % 256));
		frame.setNextByte((byte) (nof.getValue() / 256));

		frame.setNextByte(nameOfSection);

		frame.setNextByte((byte) (lengthOfSection % 0x100));
		frame.setNextByte((byte) ((lengthOfSection / 0x100) % 0x100));
		frame.setNextByte((byte) ((lengthOfSection / 0x10000) % 0x100));

		frame.setNextByte(srq);
	}

}
