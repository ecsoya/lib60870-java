package org.ecsoya.iec60870.asdu.ie.handler;

import org.ecsoya.iec60870.CP56Time2a;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.conn.IMasterConnection;

/**
 * Handler for clock synchronization command (C_CS_NA_1 - 103)
 */
@FunctionalInterface
public interface ClockSynchronizationHandler {
	boolean invoke(Object parameter, IMasterConnection connection, ASDU asdu, CP56Time2a newTime);
}