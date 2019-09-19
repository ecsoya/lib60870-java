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
package org.ecsoya.iec60870.asdu;

import org.ecsoya.iec60870.Frame;

public abstract class InformationObject {
	public static int ParseInformationObjectAddress(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		if (msg.length - startIndex < parameters.getSizeOfIOA()) {
			throw new ASDUParsingException("Message to short");
		}

		int ioa = msg[startIndex];

		if (parameters.getSizeOfIOA() > 1) {
			ioa += (msg[startIndex + 1] * 0x100);
		}

		if (parameters.getSizeOfIOA() > 2) {
			ioa += (msg[startIndex + 2] * 0x10000);
		}

		return ioa;
	}

	private int objectAddress;

	protected InformationObject(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		if (!isSequence) {
			objectAddress = ParseInformationObjectAddress(parameters, msg, startIndex);
		}
	}

	public InformationObject(int objectAddress) {
		this.objectAddress = objectAddress;
	}

	public void encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		if (!isSequence) {
			frame.setNextByte((byte) (objectAddress & 0xff));

			if (parameters.getSizeOfIOA() > 1) {
				frame.setNextByte((byte) ((objectAddress / 0x100) & 0xff));
			}

			if (parameters.getSizeOfIOA() > 2) {
				frame.setNextByte((byte) ((objectAddress / 0x10000) & 0xff));
			}
		}
	}

	/**
	 * Gets the encoded payload size of the object (information object size without
	 * the IOA)
	 *
	 * @return The encoded size in bytes
	 */
	public abstract int getEncodedSize();

	public final int getObjectAddress() {
		return this.objectAddress;
	}

	/**
	 * Indicates if this information object type supports sequence of information
	 * objects encoding
	 *
	 * <value><c>true</c> if supports sequence encoding; otherwise,
	 * <c>false</c>.</value>
	 */
	public abstract boolean getSupportsSequence();

	/**
	 * The type ID (message type) of the information object type
	 *
	 * <value>The type.</value>
	 */
	public abstract TypeID getType();

	public final void setObjectAddress(int value) {
		objectAddress = value;
	}

}
