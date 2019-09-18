/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class TestCommandWithCP56Time2a extends TestCommand {

	private final CP56Time2a time;

	private short tsc;

	public TestCommandWithCP56Time2a(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize())
			throw new ASDUParsingException("Message too small");

		tsc = msg[startIndex++];
		tsc += (short) (msg[startIndex++] * 256);

		time = new CP56Time2a(msg, startIndex);
	}

	public TestCommandWithCP56Time2a(short tsc, CP56Time2a time) {
		super();
		this.tsc = tsc;
		this.time = time;
	}

	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.setNextByte((byte) (tsc % 256));
		frame.setNextByte((byte) (tsc / 256));

		frame.appendBytes(time.getEncodedValue());
	}

	@Override
	public int getEncodedSize() {
		return 9;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_TS_TA_1;
	}
}
