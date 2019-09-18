package org.ecsoya.iec60870.asdu;

import org.ecsoya.iec60870.conn.IMasterConnection;

/**
 * Handler for ASDUs that are not handled by other handlers (default handler)
 */
@FunctionalInterface
public interface ASDUHandler {
	boolean invoke(Object parameter, IMasterConnection connection, ASDU asdu);
}