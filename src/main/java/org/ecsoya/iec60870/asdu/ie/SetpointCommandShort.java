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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.ecsoya.iec60870.Frame;
import org.ecsoya.iec60870.asdu.ASDUParsingException;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;
import org.ecsoya.iec60870.asdu.TypeID;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class SetpointCommandShort extends InformationObject {

	public static void main(String[] args) {
		byte[] bytes = { 77, 6, (byte) 158, 23 };

		System.out.println(Arrays.asList(bytes));

		float value = ByteBuffer.wrap(bytes, 0, bytes.length).order(ByteOrder.nativeOrder()).getFloat();

		System.out.println(value);

		byte[] array = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putFloat(value).array();
		System.out.println(Arrays.asList(array));
	}

	private final float value;

	private final SetpointCommandQualifier qos;

	/**
	 * @param parameters
	 * @param msg
	 * @param startIndex
	 * @param isSequence
	 * @throws ASDUParsingException
	 */
	public SetpointCommandShort(ApplicationLayerParameters parameters, byte[] msg, int startIndex)
			throws ASDUParsingException {
		super(parameters, msg, startIndex, false);
		startIndex += parameters.getSizeOfIOA(); /* skip IOA */

		if ((msg.length - startIndex) < getEncodedSize()) {
			throw new ASDUParsingException("Message too small");
		}

		/* parse float value */
		value = ByteBuffer.wrap(msg, startIndex, 4).order(ByteOrder.nativeOrder()).getFloat();
		startIndex += 4;

		this.qos = new SetpointCommandQualifier(msg[startIndex++]);
	}

	public SetpointCommandShort(int objectAddress, float value, SetpointCommandQualifier qos) {
		super(objectAddress);
		this.value = value;
		this.qos = qos;
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
		frame.appendBytes(ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putFloat(value).array());

		frame.setNextByte(this.qos.GetEncodedValue());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ecsoya.iec60870.asdu.InformationObject#GetEncodedSize()
	 */
	@Override
	public int getEncodedSize() {
		return 5;
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
		return TypeID.C_SE_NC_1;
	}
}
