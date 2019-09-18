package org.ecsoya.iec60870.asdu.ie.handler;

import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.conn.IMasterConnection;

/**
 * Handler for counter interrogation command (C_CI_NA_1 - 101).
 */
@FunctionalInterface
public interface CounterInterrogationHandler {
	boolean invoke(Object parameter, IMasterConnection connection, ASDU asdu, byte qoi);
}