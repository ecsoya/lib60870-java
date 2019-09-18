package org.ecsoya.iec60870.layer;

import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.layer.PrimaryLinkLayer.LinkLayerBusyException;

interface IPrimaryLinkLayerUnbalanced {

	void ResetCU(int slaveAddress);

	/// <summary>
	/// Determines whether this channel (slave connecrtion) is ready to transmit a
	/// new application layer message
	/// </summary>
	/// <returns><c>true</c> if this instance is channel available; otherwise,
	/// <c>false</c>.</returns>
	/// <param name="slaveAddress">link layer address of the slave</param>
	boolean IsChannelAvailable(int slaveAddress);

	void RequestClass1Data(int slaveAddress) throws LinkLayerBusyException;

	void RequestClass2Data(int slaveAddress) throws LinkLayerBusyException;

	void SendConfirmed(int slaveAddress, BufferFrame message) throws LinkLayerBusyException;

	void SendNoReply(int slaveAddress, BufferFrame message) throws LinkLayerBusyException;
}