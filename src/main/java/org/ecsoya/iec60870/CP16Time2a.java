package org.ecsoya.iec60870;

public class CP16Time2a {
	private byte[] encodedValue = new byte[2];

	public CP16Time2a(byte[] msg, int startIndex) throws ASDUParsingException {
		if (msg.length < startIndex + 3)
			throw new ASDUParsingException("Message too small for parsing CP16Time2a");

		for (int i = 0; i < 2; i++)
			encodedValue[i] = msg[startIndex + i];
	}

	public CP16Time2a(int elapsedTimeInMs) {
		encodedValue[0] = (byte) (elapsedTimeInMs % 0x100);
		encodedValue[1] = (byte) (elapsedTimeInMs / 0x100);
	}

	public int getElapsedTimeInMs() {
		return (encodedValue[0] + (encodedValue[1] * 0x100));
	}

	public byte[] getEncodedValue() {
		return encodedValue;
	}

	@Override
	public String toString() {
		return Integer.toString(getElapsedTimeInMs());
	}
}