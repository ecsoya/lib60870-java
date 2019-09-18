package org.ecsoya.iec60870.conn;

@FunctionalInterface
public interface DebugLogger {
	void invoke(String message);
}