package org.ecsoya.iec60870.layer;

import java.io.IOException;

import org.ecsoya.iec60870.ConnectionException;

public interface IPrimaryLinkLayerCallbacks {

	/// <summary>
	/// Indicate an access demand request form the client (ACD bit set in response)
	/// </summary>
	/// <param name="slaveAddress">address of the slave that requested the access
	/// demand</param>
	void accessDemand(int slaveAddress);

	/// <summary>
	/// A former request to the slave (UD Class 1, UD Class 2, confirmed...)
	/// resulted in a timeout
	/// Station does not respond indication
	/// </summary>
	/// <param name="slaveAddress">address of the slave that caused the
	/// timeout</param>
	void timeout(int slaveAddress);

	/// <summary>
	/// User data (application layer data) received from a slave
	/// </summary>
	/// <param name="slaveAddress">address of the slave that sent the data</param>
	/// <param name="message">buffer containing the received message</param>
	/// <param name="start">start of user data in the buffer</param>
	/// <param name="length">length of user data in the buffer</param>
	void userData(int slaveAddress, byte[] message, int start, int length) throws ConnectionException, IOException;
}