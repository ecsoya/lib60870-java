package org.ecsoya.iec60870.asdu.ie.handler;

import org.ecsoya.iec60870.asdu.ie.value.NameOfFile;
import org.ecsoya.iec60870.conn.IFileReceiver;

/**
 * File ready handler. Will be called by the slave when a master sends a FILE
 * READY (file download announcement) message to the slave.
 */
@FunctionalInterface
public interface FileReadyHandler {
	IFileReceiver invoke(Object parameter, int ca, int ioa, NameOfFile nof, int lengthOfFile);
}