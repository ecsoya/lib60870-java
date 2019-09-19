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

import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class TestCommand extends InformationObject {

	private boolean valid = true;

	public TestCommand() {
		super(0);
	}

	/**
	 * @param parameters
	 * @param msg
	 * @param startIndex
	 * @param isSequence
	 * @throws ASDUParsingException
	 */
	public TestCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex) throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		if (msg[startIndex++] != 0xcc) {
			valid = false;
		}

		if (msg[startIndex] != 0x55) {
			valid = false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ecsoya.iec60870.asdu.InformationObject#Encode(org.ecsoya.iec60870.Frame,
	 * org.ecsoya.iec60870.asdu.ApplicationLayerParameters, boolean)
	 */
	@Override
	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		super.encode(frame, parameters, isSequence);
		frame.setNextByte((byte) 0xcc);
		frame.setNextByte((byte) 0x55);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ecsoya.iec60870.asdu.InformationObject#GetEncodedSize()
	 */
	@Override
	public int getEncodedSize() {
		return 2;
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
		return TypeID.C_TS_NA_1;
	}

	public boolean isValid() {
		return valid;
	}

}
