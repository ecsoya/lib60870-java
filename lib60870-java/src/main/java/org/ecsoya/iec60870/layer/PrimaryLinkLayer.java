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

import org.ecsoya.iec60870.core.ConnectionException;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public abstract class PrimaryLinkLayer {

	public static class LinkLayerBusyException extends ConnectionException {
		private static final long serialVersionUID = 7102209933712617545L;

		public LinkLayerBusyException(String message) {
			super(message);
		}

		public LinkLayerBusyException(String message, Exception e) {
			super(message, e);
		}
	}

	public abstract void handleMessage(FunctionCodeSecondary fcs, boolean dir, boolean dfc, int address, byte[] msg,
			int userDataStart, int userDataLength) throws IOException, Exception;

	public abstract void runStateMachine() throws IOException;

	public abstract void sendLinkLayerTestFunction();

}
