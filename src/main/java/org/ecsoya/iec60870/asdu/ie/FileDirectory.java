/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class FileDirectory extends InformationObject {
	private NameOfFile nof;
	private int lengthOfFile;
	private byte sof; /* Status of file (7.2.6.38) */

	private CP56Time2a creationTime;

	public FileDirectory(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence)
			startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize())
			throw new ASDUParsingException("Message too small");

		int nofValue;

		nofValue = msg[startIndex++];
		nofValue += (msg[startIndex++] * 0x100);

		nof = NameOfFile.forValue(nofValue);

		lengthOfFile = msg[startIndex++];
		lengthOfFile += (msg[startIndex++] * 0x100);
		lengthOfFile += (msg[startIndex++] * 0x10000);

		sof = msg[startIndex++];

		/* parse CP56Time2a (creation time of file) */
		creationTime = new CP56Time2a(msg, startIndex);
	}

	public FileDirectory(int objectAddress, NameOfFile nof, int lengthOfFile, byte sof, CP56Time2a creationTime) {
		super(objectAddress);
		this.nof = nof;
		this.lengthOfFile = lengthOfFile;
		this.sof = sof;
		this.creationTime = creationTime;
	}

	public FileDirectory(int objectAddress, NameOfFile nof, int lengthOfFile, int status, boolean LFD, boolean FOR,
			boolean FA, CP56Time2a creationTime) {
		super(objectAddress);
		this.nof = nof;
		this.lengthOfFile = lengthOfFile;

		if (status < 0)
			status = 0;
		else if (status > 31)
			status = 31;

		byte sof = (byte) status;

		if (LFD)
			sof += 0x20;

		if (FOR)
			sof += 0x40;

		if (FA)
			sof += 0x80;

		this.sof = sof;
		this.creationTime = creationTime;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.setNextByte((byte) (nof.getValue() % 256));
		frame.setNextByte((byte) (nof.getValue() / 256));

		frame.setNextByte((byte) (lengthOfFile % 0x100));
		frame.setNextByte((byte) ((lengthOfFile / 0x100) % 0x100));
		frame.setNextByte((byte) ((lengthOfFile / 0x10000) % 0x100));

		frame.setNextByte((byte) sof);

		frame.appendBytes(creationTime.getEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 13;
	}

	public NameOfFile getNof() {
		return nof;
	}

	public int getStatus() {
		return (int) (sof & 0x1f);
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.F_DR_TA_1;
	}

	public boolean isFA() {
		return ((sof & 0x80) == 0x80);
	}

	public boolean isFOR() {
		return ((sof & 0x40) == 0x40);
	}

	public boolean isLFD() {
		return ((sof & 0x20) == 0x20);
	}
}
