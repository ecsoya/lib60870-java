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
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

public class ReadCommand extends InformationObject {
	public ReadCommand(ApplicationLayerParameters parameters, byte[] msg, int startIndex) throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
	}

	public ReadCommand(int ioa) {
		super(ioa);
	}

	@Override
	public int getEncodedSize() {
		return 0;
	}

	@Override
	public boolean getSupportsSequence() {
		return false;
	}

	@Override
	public TypeID getType() {
		return TypeID.C_RD_NA_1;
	}
}
