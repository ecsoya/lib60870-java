package org.ecsoya.iec60870.layer;

import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.layer.PrimaryLinkLayer.LinkLayerBusyException;

interface IPrimaryLinkLayerUnbalanced {

	/// <summary>
	/// Determines whether this channel (slave connecrtion) is ready to transmit a
	/// new application layer message
	/// </summary>
	/// <returns><c>true</c> if this instance is channel available; otherwise,
	/// <c>false</c>.</returns>
	/// <param name="slaveAddress">link layer address of the slave</param>
	boolean isChannelAvailable(int slaveAddress);

	void requestClass1Data(int slaveAddress) throws LinkLayerBusyException;

	void requestClass2Data(int slaveAddress) throws LinkLayerBusyException;

	void resetCU(int slaveAddress);

	void sendConfirmed(int slaveAddress, BufferFrame message) throws LinkLayerBusyException;

	void sendNoReply(int slaveAddress, BufferFrame message) throws LinkLayerBusyException;
}