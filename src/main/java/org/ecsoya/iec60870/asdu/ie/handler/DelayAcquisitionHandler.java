package org.ecsoya.iec60870.asdu.ie.handler;

import org.ecsoya.iec60870.CP16Time2a;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.conn.IMasterConnection;

/**
 * Handler for delay acquisition command (C_CD_NA:1 - 106)
 */
@FunctionalInterface
public interface DelayAcquisitionHandler {
	boolean invoke(Object parameter, IMasterConnection connection, ASDU asdu, CP16Time2a delayTime);
}