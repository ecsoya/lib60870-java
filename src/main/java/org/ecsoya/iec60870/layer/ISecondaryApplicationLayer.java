package org.ecsoya.iec60870.layer;

import org.ecsoya.iec60870.BufferFrame;

public interface ISecondaryApplicationLayer {
	boolean IsClass1DataAvailable();

	BufferFrame GetClass1Data();

	BufferFrame GetCLass2Data();

	boolean HandleReceivedData(byte[] msg, boolean isBroadcast, int userDataStart, int userDataLength);

	void ResetCUReceived(boolean onlyFCB);
}