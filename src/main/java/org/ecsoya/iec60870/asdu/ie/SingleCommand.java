package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class SingleCommand extends InformationObject {
	@Override
	public int GetEncodedSize() {
		return 1;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_SC_NA_1;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private byte sco;
	private byte sco;

	public SingleCommand(int ioa, boolean command, boolean selectCommand, int qu) {
		super(ioa);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: sco = (byte)((qu & 0x1f) * 4);
		sco = (byte) ((qu & 0x1f) * 4);

		if (command) {
			sco |= 0x01;
		}

		if (selectCommand) {
			sco |= 0x80;
		}
	}

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal SingleCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
	public SingleCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); // skip IOA

		if ((msg.length - startIndex) < GetEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		sco = msg[startIndex++];
	}

	@Override
	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.Encode(frame, parameters, isSequence);

		frame.setNextByte(sco);
	}

	public final int getQU() {
		return ((sco & 0x7c) / 4);
	}

	public final void setQU(int value) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: sco = (byte)(sco & 0x81);
		sco = (byte) (sco & 0x81);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: sco += (byte)((value & 0x1f) * 4);
		sco += (byte) ((value & 0x1f) * 4);
	}

	/**
	 * Gets the state (off - false / on - true) of this command
	 * 
	 * <value><c>true</c> if on; otherwise, <c>false</c>.</value>
	 */
	public final boolean getState() {
		return ((sco & 0x01) == 0x01);
	}

	public final void setState(boolean value) {
		if (value) {
			sco |= 0x01;
		} else {
			sco &= 0xfe;
		}
	}

	/**
	 * Indicates if the command is a select or an execute command
	 * 
	 * <value><c>true</c> if select; execute, <c>false</c>.</value>
	 */
	public final boolean getSelect() {
		return ((sco & 0x80) == 0x80);
	}

	public final void setSelect(boolean value) {
		if (value) {
			sco |= 0x80;
		} else {
			sco &= 0x7f;
		}
	}

	@Override
	public String toString() {
		return String.format("[SingleCommand: QU=%1$s, State=%2$s, Select=%3$s]", getQU(), getState(), getSelect());
	}

}