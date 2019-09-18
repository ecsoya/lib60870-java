package org.ecsoya.iec60870.conn;

import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.asdu.InformationObject;

public interface IPrivateIOFactory {
	/**
	 * Decode the information object and create a new InformationObject instance
	 * 
	 * @param parameters Application layer parameters required for decoding
	 * @param msg        the received message
	 * @param startIndex start index of the payload in the message
	 * @param isSequence If set to <c>true</c> is sequence.
	 */
	InformationObject decode(ApplicationLayerParameters parameters, byte[] msg, int startIndex, boolean isSequence);

	/**
	 * Gets the encoded payload size of the object (information object size without
	 * the IOA)
	 * 
	 * @return The encoded size in bytes
	 */
	int getEncodedSize();
}