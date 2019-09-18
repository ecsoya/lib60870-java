/**
 * 
 */
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.TypeID;
import org.ecsoya.iec60870.asdu.ie.value.StepCommandValue;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class StepCommand extends DoubleCommand {

	private final StepCommandValue value;

	/**
	 * @param parameters
	 * @param msg
	 * @param startIndex
	 * @throws ASDUParsingException
	 */
	public StepCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex) throws ASDUParsingException {
		super(parameters, msg, startIndex);
		value = StepCommandValue.forValue(getState());
	}

	/**
	 * @param objectAddress
	 * @param command
	 * @param select
	 * @param quality
	 */
	public StepCommand(int objectAddress, int command, boolean select, int quality) {
		super(objectAddress, command, select, quality);
		value = StepCommandValue.forValue(getState());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.DoubleCommand#getType()
	 */
	@Override
	public TypeID getType() {
		return TypeID.C_RC_NA_1;
	}

	public StepCommandValue getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ecsoya.iec60870.asdu.ie.DoubleCommand#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return false;
	}

}
