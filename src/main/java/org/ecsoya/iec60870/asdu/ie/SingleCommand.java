package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class SingleCommand extends InformationObject {
	private byte sco;

	public SingleCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		sco = msg[startIndex++];
	}

	public SingleCommand(int ioa, boolean command, boolean selectCommand, int qu) {
		super(ioa);
		sco = (byte) ((qu & 0x1f) * 4);

		if (command) {
			sco |= 0x01;
		}

		if (selectCommand) {
			sco |= 0x80;
		}
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);

		frame.setNextByte(sco);
	}

	@Override
	public int getEncodedSize() {
		return 1;
	}

	public final int getQU() {
		return ((sco & 0x7c) / 4);
	}

	/**
	 * Indicates if the command is a select or an execute command
	 * 
	 * <value><c>true</c> if select; execute, <c>false</c>.</value>
	 */
	public final boolean getSelect() {
		return ((sco & 0x80) == 0x80);
	}

	/**
	 * Gets the state (off - false / on - true) of this command
	 * 
	 * <value><c>true</c> if on; otherwise, <c>false</c>.</value>
	 */
	public final boolean getState() {
		return ((sco & 0x01) == 0x01);
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_SC_NA_1;
	}

	public final void setQU(int value) {
		sco = (byte) (sco & 0x81);
		sco += (byte) ((value & 0x1f) * 4);
	}

	public final void setSelect(boolean value) {
		if (value) {
			sco |= 0x80;
		} else {
			sco &= 0x7f;
		}
	}

	public final void setState(boolean value) {
		if (value) {
			sco |= 0x01;
		} else {
			sco &= 0xfe;
		}
	}

	@Override
	public String toString() {
		return String.format("[SingleCommand: QU=%1$s, State=%2$s, Select=%3$s]", getQU(), getState(), getSelect());
	}

}