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
public class FileSegment extends InformationObject {
	private static int ENCODED_SIZE = 4;
	private NameOfFile nof;
	private byte nameOfSection;

	private byte los; /* length of Segment */

	private byte[] data = null;

	public FileSegment(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
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

		los = msg[startIndex++];

		if (los > GetMaxDataSize(parameters))
			throw new ASDUParsingException("Payload data too large");

		if ((msg.length - startIndex) < los)
			throw new ASDUParsingException("Message too small");

		data = new byte[los];

		for (int i = 0; i < los; i++) {
			data[i] = msg[startIndex++];
		}
	}

	public FileSegment(int objectAddress, NameOfFile nof, byte nameOfSection, byte[] data) {
		super(objectAddress);
		this.nof = nof;
		this.nameOfSection = nameOfSection;
		this.data = data;
	}

	@Override
	public int GetEncodedSize() {
		return ENCODED_SIZE;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.F_SG_NA_1;
	}

	public byte getNameOfSection() {
		return nameOfSection;
	}

	public NameOfFile getNof() {
		return nof;
	}

	public byte[] getData() {
		return data;
	}

	public byte getLos() {
		return los;
	}

	public static int GetMaxDataSize(ApplicationLayerParameters parameters) {
		int maxSize = parameters.getMaxAsduLength() - parameters.getSizeOfTypeId() - parameters.getSizeOfVSQ()
				- parameters.getSizeOfCA() - parameters.getSizeOfCOT() - parameters.getSizeOfIOA() - ENCODED_SIZE;

		return maxSize;
	}

	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);
		frame.setNextByte((byte) (nof.getValue() % 256));
		frame.setNextByte((byte) (nof.getValue() / 256));

		frame.setNextByte(nameOfSection);

		frame.setNextByte(los);

		if (data.length > GetMaxDataSize(parameters)) {
			throw new RuntimeException("Payload data too large");
		} else {
			frame.appendBytes(data);
		}
	}
}
