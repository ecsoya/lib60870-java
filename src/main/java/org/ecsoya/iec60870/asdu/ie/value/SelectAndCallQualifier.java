package org.ecsoya.iec60870.asdu.ie.value;

public enum SelectAndCallQualifier {
	DEFAULT((byte) 0),
	SELECT_FILE((byte) 1),
	REQUEST_FILE((byte) 2),
	DEACTIVATE_FILE((byte) 3),
	DELETE_FILE((byte) 4),
	SELECT_SECTION((byte) 5),
	REQUEST_SECTION((byte) 6),
	DEACTIVATE_SECTION((byte) 7);

	public static final int SIZE = java.lang.Byte.SIZE;

	private byte byteValue;
	private static java.util.HashMap<Byte, SelectAndCallQualifier> mappings;

	private static java.util.HashMap<Byte, SelectAndCallQualifier> getMappings() {
		if (mappings == null) {
			synchronized (SelectAndCallQualifier.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Byte, SelectAndCallQualifier>();
				}
			}
		}
		return mappings;
	}

	private SelectAndCallQualifier(byte value) {
		byteValue = value;
		getMappings().put(value, this);
	}

	public byte getValue() {
		return byteValue;
	}

	public static SelectAndCallQualifier forValue(byte value) {
		return getMappings().get(value);
	}
}