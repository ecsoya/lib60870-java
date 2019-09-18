package org.ecsoya.iec60870.cs104;

import org.ecsoya.iec60870.asdu.ASDU;

/**
 * ASDU received handler.
 */
@FunctionalInterface
public interface ASDUReceivedHandler {
	boolean invoke(Object parameter, ASDU asdu);
}