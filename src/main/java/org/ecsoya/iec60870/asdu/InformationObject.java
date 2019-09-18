package org.ecsoya.iec60870.asdu;

import org.ecsoya.iec60870.ASDUParsingException;
import org.ecsoya.iec60870.Frame;

/*
 *  InformationObject.cs
 *
 *  Copyright 2016 MZ Automation GmbH
 *
 *  This file is part of lib60870.NET
 *
 *  lib60870.NET is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib60870.NET is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with lib60870.NET.  If not, see <http: //www.gnu.org/licenses/>.
 *
 *  See COPYING file for the complete license text.
 */

public abstract class InformationObject {
	private int objectAddress;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: internal static int ParseInformationObjectAddress(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: protected InformationObject(ApplicationLayerParameters parameters, byte[] msg, int startIndex, bool isSequence)
	protected InformationObject(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence)
			throws ASDUParsingException {
		if (!isSequence) {
			objectAddress = ParseInformationObjectAddress(parameters, msg, startIndex);
		}
	}

	public InformationObject(int objectAddress) {
		this.objectAddress = objectAddress;
	}

	/**
	 * Gets the encoded payload size of the object (information object size without
	 * the IOA)
	 * 
	 * @return The encoded size in bytes
	 */
	public abstract int GetEncodedSize();

	public final int getObjectAddress() {
		return this.objectAddress;
	}

	public final void setObjectAddress(int value) {
		objectAddress = value;
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

	public void Encode(Frame frame, ApplicationLayerParameters parameters, boolean isSequence) {
		if (!isSequence) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: frame.SetNextByte((byte)(objectAddress & 0xff));
			frame.setNextByte((byte) (objectAddress & 0xff));

			if (parameters.getSizeOfIOA() > 1) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: frame.SetNextByte((byte)((objectAddress / 0x100) & 0xff));
				frame.setNextByte((byte) ((objectAddress / 0x100) & 0xff));
			}

			if (parameters.getSizeOfIOA() > 2) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: frame.SetNextByte((byte)((objectAddress / 0x10000) & 0xff));
				frame.setNextByte((byte) ((objectAddress / 0x10000) & 0xff));
			}
		}
	}

}