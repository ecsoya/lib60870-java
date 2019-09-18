/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class DoubleCommand extends InformationObject {
	public static int OFF = 1;
	public static int ON = 2;
	private byte dcq;

	/**
	 * @param parameters
	 * @param msg
	 * @param startIndex
	 * @param isSequence
	 * @throws ASDUParsingException
	 */
	public DoubleCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < GetEncodedSize())
			throw new ASDUParsingException("Message too small");

		dcq = msg[startIndex++];
	}

	/**
	 * @param objectAddress
	 */
	public DoubleCommand(int objectAddress, int command, boolean select, int quality) {
		super(objectAddress);
		dcq = (byte) (command & 0x03);
		dcq += (byte) ((quality & 0x1f) * 4);

		if (select) {
			dcq |= 0x80;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.InformationObject#GetEncodedSize()
	 */
	@Override
	public int GetEncodedSize() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.InformationObject#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.InformationObject#getType()
	 */
	@Override
	public TypeID getType() {
		return TypeID.C_DC_NA_1;
	}

	public int getQU() {
		return ((dcq & 0x7c) / 4);
	}

	public int getState() {
		return (dcq & 0x03);
	}

	public boolean isSelect() {
		return ((dcq & 0x80) == 0x80);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ecsoya.iec60870.asdu.InformationObject#Encode(org.ecsoya.iec60870.Frame,
	 * org.ecsoya.iec60870.asdu.ApplicationLayerParameters, boolean)
	 */
	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);
		frame.setNextByte(dcq);
	}
}
