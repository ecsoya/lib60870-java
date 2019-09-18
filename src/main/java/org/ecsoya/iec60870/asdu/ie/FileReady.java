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
public class FileReady extends InformationObject {

	private NameOfFile nof;

	private int lengthOfFile;

	private byte frq;// File ready qualifier

	public FileReady(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
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

		lengthOfFile = msg[startIndex++];
		lengthOfFile += (msg[startIndex++] * 0x100);
		lengthOfFile += (msg[startIndex++] * 0x10000);

		/* parse FRQ (file ready qualifier) */
		frq = msg[startIndex++];
	}

	public FileReady(int objectAddress, NameOfFile nof, int lengthOfFile, boolean positive) {
		super(objectAddress);
		this.nof = nof;
		this.lengthOfFile = lengthOfFile;

		if (positive)
			frq = (byte) 0x80;
		else
			frq = 0;
	}

	public boolean isPositive() {
		return ((frq & 0x80) == 0x80);
	}

	@Override
	public int GetEncodedSize() {
		return 6;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.F_FR_NA_1;
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);
		frame.setNextByte((byte) (nof.getValue() % 256));
		frame.setNextByte((byte) (nof.getValue() / 256));

		frame.setNextByte((byte) (lengthOfFile % 0x100));
		frame.setNextByte((byte) ((lengthOfFile / 0x100) % 0x100));
		frame.setNextByte((byte) ((lengthOfFile / 0x10000) % 0x100));

		frame.setNextByte(frq);
	}
}
