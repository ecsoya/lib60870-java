package org.ecsoya.iec60870.asdu.ie.value;

public enum FileClientState {
	IDLE,
	WAITING_FOR_FILE_READY,
	WAITING_FOR_SECTION_READY, // or for LAST_SECTION
	RECEIVING_SECTION; // waiting for SEGMENT or LAST SEGMENT

	public static final int SIZE = java.lang.Integer.SIZE;

	public static FileClientState forValue(int value) {
		return values()[value];
	}

	public int getValue() {
		return this.ordinal();
	}
}