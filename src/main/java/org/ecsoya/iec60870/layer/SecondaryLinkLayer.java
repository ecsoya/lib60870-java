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
package org.ecsoya.iec60870.layer;

import java.io.IOException;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public abstract class SecondaryLinkLayer {

	private int Address;

	/**
	 * @return the address
	 */
	public int getAddress() {
		return Address;
	}

	public abstract void handleMessage(FunctionCodePrimary fcp, boolean isBroadcast, int address, boolean fcb,
			boolean fcv, byte[] msg, int userDataStart, int userDataLength) throws IOException;

	public abstract void runStateMachine();

	/**
	 * @param address the address to set
	 */
	public void setAddress(int address) {
		Address = address;
	}

}
