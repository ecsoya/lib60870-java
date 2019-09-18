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
import org.ecsoya.iec60870.asdu.ie.value.SelectAndCallQualifier;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class FileCallOrSelect extends InformationObject {

	private final NameOfFile nof;
	private final byte nameOfSection;

	private final SelectAndCallQualifier scq;

	public FileCallOrSelect(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
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

		nameOfSection = msg[startIndex++];

		/* parse SCQ (select and call qualifier) */
		scq = SelectAndCallQualifier.forValue(msg[startIndex++]);
	}

	public FileCallOrSelect(int objectAddress, NameOfFile nof, byte nameOfSection, SelectAndCallQualifier scq) {
		super(objectAddress);
		this.nof = nof;
		this.nameOfSection = nameOfSection;
		this.scq = scq;

	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.setNextByte((byte) (nof.getValue() % 256));
		frame.setNextByte((byte) (nof.getValue() / 256));

		frame.setNextByte(nameOfSection);

		frame.setNextByte((byte) scq.getValue());
	}

	@Override
	public int getEncodedSize() {
		return 4;
	}

	public byte getNameOfSection() {
		return nameOfSection;
	}

	public NameOfFile getNof() {
		return nof;
	}

	public SelectAndCallQualifier getScq() {
		return scq;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.F_SC_NA_1;
	}
}
