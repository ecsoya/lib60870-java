package org.ecsoya.iec60870.asdu.ie.value;

public enum FileError {
	DEFAULT(0), // no error
	REQ_MEMORY_NOT_AVAILABLE(1),
	CHECKSUM_FAILED(2),
	UNEXPECTED_COMM_SERVICE(3),
	UNEXPECTED_NAME_OF_FILE(4),
	UNEXPECTED_NAME_OF_SECTION(5);
	private static java.util.HashMap<Integer, FileError> mappings;

	public static FileError forValue(int value) {
		return getMappings().get(value);
	}

	private static java.util.HashMap<Integer, FileError> getMappings() {
		if (mappings == null) {
			synchronized (NameOfFile.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, FileError>();
				}
			}
		}
		return mappings;
	}

	private int shortValue;

	private FileError(int value) {
		shortValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return shortValue;
	}
}