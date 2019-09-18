package org.ecsoya.iec60870.cs104;

/// <summary>
/// Server mode (redundancy group support)
/// </summary>
public enum ServerMode {
	/// <summary>
	/// There is only one redundancy group. There can only be one active
	/// connections.
	/// All other connections are standy connections.
	/// </summary>
	SINGLE_REDUNDANCY_GROUP,
	/// <summary>
	/// Every connection is an own redundancy group. This enables simple
	/// multi-client server.
	/// </summary>
	CONNECTION_IS_REDUNDANCY_GROUP
}
