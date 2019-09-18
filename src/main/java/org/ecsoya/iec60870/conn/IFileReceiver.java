package org.ecsoya.iec60870.conn;

import org.ecsoya.iec60870.asdu.ie.value.FileErrorCode;

public interface IFileReceiver {
	void Finished(FileErrorCode result);

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void SegmentReceived(byte sectionName, int offset, int size, byte[] data);
	void SegmentReceived(byte sectionName, int offset, int size, byte[] data);
}