package org.ecsoya.iec60870.cs104;

/// <summary>
/// Connection events for the Server
/// </summary>
public enum ClientConnectionEvent {
	/// <summary>
	/// A new connection is opened
	/// </summary>
	OPENED,

	/// <summary>
	/// The connection entered active state
	/// </summary>
	ACTIVE,

	/// <summary>
	/// The connection enterend inactive state
	/// </summary>
	INACTIVE,

	/// <summary>
	/// The connection is closed
	/// </summary>
	CLOSED
}
