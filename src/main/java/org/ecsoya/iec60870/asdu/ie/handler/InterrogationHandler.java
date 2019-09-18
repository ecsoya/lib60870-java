package org.ecsoya.iec60870.asdu.ie.handler;

import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.conn.IMasterConnection;

/**
 * Handler for interrogation command (C_IC_NA_1 - 100).
 */
@FunctionalInterface
public interface InterrogationHandler {
	boolean invoke(Object parameter, IMasterConnection connection, ASDU asdu, byte qoi);
}