package org.ecsoya.iec60870.asdu;

public class ApplicationLayerParameters implements Cloneable {
	public static int IEC60870_5_104_MAX_ASDU_LENGTH = 249;

	private int sizeOfTypeId = 1;

	private int sizeOfVSQ = 1; // VSQ = variable sturcture qualifier

	private int sizeOfCOT = 2; // (parameter b) COT = cause of transmission (1/2)

	private int originatorAddress = 0;

	private int sizeOfCA = 2; // (parameter a) CA = common address of ASDUs (1/2)

	private int sizeOfIOA = 3; // (parameter c) IOA = information object address (1/2/3)

	private int maxAsduLength = IEC60870_5_104_MAX_ASDU_LENGTH; // maximum length of ASDU

	public ApplicationLayerParameters() {
	}

	public ApplicationLayerParameters clone() {
		ApplicationLayerParameters copy = new ApplicationLayerParameters();

		copy.sizeOfTypeId = sizeOfTypeId;
		copy.sizeOfVSQ = sizeOfVSQ;
		copy.sizeOfCOT = sizeOfCOT;
		copy.originatorAddress = originatorAddress;
		copy.sizeOfCA = sizeOfCA;
		copy.sizeOfIOA = sizeOfIOA;
		copy.maxAsduLength = maxAsduLength;

		return copy;
	}

	public final int getMaxAsduLength() {
		return this.maxAsduLength;
	}

	public final int getOA() {
		return this.originatorAddress;
	}

	public final int getSizeOfCA() {
		return this.sizeOfCA;
	}

	public final int getSizeOfCOT() {
		return this.sizeOfCOT;
	}

	public final int getSizeOfIOA() {
		return this.sizeOfIOA;
	}

	public final int getSizeOfTypeId() {
		return this.sizeOfTypeId;
	}

	public final int getSizeOfVSQ() {
		return this.sizeOfVSQ;
	}

	public final void setMaxAsduLength(int value) {
		maxAsduLength = value;
	}

	public final void setOA(int value) {
		originatorAddress = value;
	}

	public final void setSizeOfCA(int value) {
		sizeOfCA = value;
	}

	public final void setSizeOfCOT(int value) {
		sizeOfCOT = value;
	}

	public final void setSizeOfIOA(int value) {
		sizeOfIOA = value;
	}
}