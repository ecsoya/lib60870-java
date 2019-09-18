package org.ecsoya.iec60870.conn;

import org.ecsoya.iec60870.asdu.ie.value.FileErrorCode;

public interface IFileReceiver {
	void finished(FileErrorCode result);

	void segmentReceived(byte sectionName, int offset, int size, byte[] data);
}