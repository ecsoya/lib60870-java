package org.ecsoya.iec60870.layer;

import org.ecsoya.iec60870.BufferFrame;

public interface ISecondaryApplicationLayer {
	BufferFrame getClass1Data();

	BufferFrame getCLass2Data();

	boolean handleReceivedData(byte[] msg, boolean isBroadcast, int userDataStart, int userDataLength);

	boolean isClass1DataAvailable();

	void resetCUReceived(boolean onlyFCB);
}