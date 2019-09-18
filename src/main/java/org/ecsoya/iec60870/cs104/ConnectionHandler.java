package org.ecsoya.iec60870.cs104;

/**
 * Callback handler for connection events
 */
@FunctionalInterface
public interface ConnectionHandler {
	void invoke(Object parameter, ConnectionEvent connectionEvent);
}