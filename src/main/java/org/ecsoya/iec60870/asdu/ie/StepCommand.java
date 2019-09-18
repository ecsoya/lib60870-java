/*******************************************************************************
 * Copyright (C) 2019 Ecsoya (jin.liu@soyatec.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.ecsoya.iec60870.asdu.ie;

import org.ecsoya.iec60870.asdu.ASDUParsingException;
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
	 * @see org.ecsoya.iec60870.asdu.ie.DoubleCommand#getSupportsSequence()
	 */
	@Override
	public boolean getSupportsSequence() {
		return false;
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

}
