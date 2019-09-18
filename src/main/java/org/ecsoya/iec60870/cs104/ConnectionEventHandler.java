package org.ecsoya.iec60870.cs104;

@FunctionalInterface
public interface ConnectionEventHandler {
	void invoke(Object parameter, ClientConnection connection, ClientConnectionEvent eventType);
}