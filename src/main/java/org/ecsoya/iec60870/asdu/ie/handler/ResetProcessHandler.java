package org.ecsoya.iec60870.asdu.ie.handler;

import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.conn.IMasterConnection;

/**
 * Handler for reset process command (C_RP_NA_1 - 105)
 */
@FunctionalInterface
public interface ResetProcessHandler {
	boolean invoke(Object parameter, IMasterConnection connection, ASDU asdu, byte qrp);
}