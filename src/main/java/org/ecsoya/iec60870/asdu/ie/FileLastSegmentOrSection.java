/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.LastSectionOrSegmentQualifier;
import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class FileLastSegmentOrSection extends InformationObject {
	private final NameOfFile nof;
	private final byte nameOfSection;

	private final LastSectionOrSegmentQualifier lsq;

	private final byte chs;

	public FileLastSegmentOrSection(ApplicationLayerParameters parameters, byte[] msg, int startIndex,
			boolean isSequence) throws ASDUParsingException {
		super(parameters, msg, startIndex, isSequence);
		if (!isSequence)
			startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize())
			throw new ASDUParsingException("Message too small");

		int nofValue;

		nofValue = msg[startIndex++];
		nofValue += (msg[startIndex++] * 0x100);

		nof = NameOfFile.forValue(nofValue);

		nameOfSection = msg[startIndex++];

		/* parse LSQ (last section or segment qualifier) */
		lsq = LastSectionOrSegmentQualifier.forValue(msg[startIndex++]);

		chs = msg[startIndex++];
	}

	public FileLastSegmentOrSection(int objectAddress, NameOfFile nof, byte nameOfSection,
			LastSectionOrSegmentQualifier lsq, byte checksum) {
		super(objectAddress);
		this.nof = nof;
		this.nameOfSection = nameOfSection;
		this.lsq = lsq;
		this.chs = checksum;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.setNextByte((byte) (nof.getValue() % 256));
		frame.setNextByte((byte) (nof.getValue() / 256));

		frame.setNextByte(nameOfSection);

		frame.setNextByte(lsq.getValue());
		frame.setNextByte(chs);
	}

	public byte getChs() {
		return chs;
	}

	@Override
	public int getEncodedSize() {
		return 5;
	}

	public LastSectionOrSegmentQualifier getLsq() {
		return lsq;
	}

	public byte getNameOfSection() {
		return nameOfSection;
	}

	public NameOfFile getNof() {
		return nof;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.F_LS_NA_1;
	}
}
