package org.ecsoya.iec60870.asdu.ie.handler;

import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.conn.IMasterConnection;

/**
 * Handler for read command (C_RD_NA_1 - 102)
 */
@FunctionalInterface
public interface ReadHandler {
	boolean invoke(Object parameter, IMasterConnection connection, ASDU asdu, int ioa);
}