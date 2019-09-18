package org.ecsoya.iec60870.asdu.ie.value;

public enum AcknowledgeQualifier {
	NOT_USED(0),
	POS_ACK_FILE(1),
	NEG_ACK_FILE(2),
	POS_ACK_SECTION(3),
	NEG_ACK_SECTION(4);

	public static final int SIZE = java.lang.Integer.SIZE;

	private static java.util.HashMap<Integer, AcknowledgeQualifier> mappings;

	public static AcknowledgeQualifier forValue(int value) {
		return getMappings().get(value);
	}

	private static java.util.HashMap<Integer, AcknowledgeQualifier> getMappings() {
		if (mappings == null) {
			synchronized (AcknowledgeQualifier.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, AcknowledgeQualifier>();
				}
			}
		}
		return mappings;
	}

	private int intValue;

	private AcknowledgeQualifier(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}
}