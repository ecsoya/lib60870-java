package org.ecsoya.iec60870.asdu.ie.value;

public enum LastSectionOrSegmentQualifier {
	NOT_USED((byte) 0),
	FILE_TRANSFER_WITHOUT_DEACT((byte) 1),
	FILE_TRANSFER_WITH_DEACT((byte) 2),
	SECTION_TRANSFER_WITHOUT_DEACT((byte) 3),
	SECTION_TRANSFER_WITH_DEACT((byte) 4);

	public static final int SIZE = java.lang.Byte.SIZE;

	private byte byteValue;
	private static java.util.HashMap<Byte, LastSectionOrSegmentQualifier> mappings;

	private static java.util.HashMap<Byte, LastSectionOrSegmentQualifier> getMappings() {
		if (mappings == null) {
			synchronized (LastSectionOrSegmentQualifier.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Byte, LastSectionOrSegmentQualifier>();
				}
			}
		}
		return mappings;
	}

	private LastSectionOrSegmentQualifier(byte value) {
		byteValue = value;
		getMappings().put(value, this);
	}

	public byte getValue() {
		return byteValue;
	}

	public static LastSectionOrSegmentQualifier forValue(byte value) {
		return getMappings().get(value);
	}
}