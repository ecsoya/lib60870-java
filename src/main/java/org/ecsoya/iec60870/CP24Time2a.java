package org.ecsoya.iec60870;

public class CP24Time2a {
	private byte[] encodedValue = new byte[3];

	public CP24Time2a(byte[] msg, int startIndex) throws ASDUParsingException {
		if (msg.length < startIndex + 3)
			throw new ASDUParsingException("Message too small for parsing CP24Time2a");

		for (int i = 0; i < 3; i++)
			encodedValue[i] = msg[startIndex + i];
	}

	public CP24Time2a() {
		for (int i = 0; i < 3; i++)
			encodedValue[i] = 0;
	}

	public CP24Time2a(int minute, int second, int millisecond) {
		setMillisecond(millisecond);
		setSecond(second);
		setMinute(minute);
	}

	/// <summary>
	/// Gets the total milliseconds of the elapsed time
	/// </summary>
	/// <returns>The milliseconds.</returns>
	public int getMilliseconds() {

		int millies = getMinute() * (60000) + getSecond() * 1000 + getMillisecond();

		return millies;
	}

	/// <summary>
	/// Gets or sets the millisecond part of the time value
	/// </summary>
	/// <value>The millisecond.</value>
	public int getMillisecond() {
		return (encodedValue[0] + (encodedValue[1] * 0x100)) % 1000;
	}

	public void setMillisecond(int value) {
		int millies = (getSecond() * 1000) + value;

		encodedValue[0] = (byte) (millies & 0xff);
		encodedValue[1] = (byte) ((millies / 0x100) & 0xff);
	}

	/// <summary>
	/// Gets or sets the second (range 0 to 59)
	/// </summary>
	/// <value>The second.</value>
	public int getSecond() {
		return (encodedValue[0] + (encodedValue[1] * 0x100)) / 1000;
	}

	public void setSecond(int value) {
		int millies = encodedValue[0] + (encodedValue[1] * 0x100);

		int msPart = millies % 1000;

		millies = (value * 1000) + msPart;

		encodedValue[0] = (byte) (millies & 0xff);
		encodedValue[1] = (byte) ((millies / 0x100) & 0xff);
	}

	/// <summary>
	/// Gets or sets the minute (range 0 to 59)
	/// </summary>
	/// <value>The minute.</value>
	public int getMinute() {
		return (encodedValue[2] & 0x3f);
	}

	public void setMinute(int value) {
		encodedValue[2] = (byte) ((encodedValue[2] & 0xc0) | (value & 0x3f));
	}

	/// <summary>
	/// Gets a value indicating whether this <see cref="lib60870.CP24Time2a"/> is
	/// invalid.
	/// </summary>
	/// <value><c>true</c> if invalid; otherwise, <c>false</c>.</value>
	public boolean isInvalid() {
		return ((encodedValue[2] & 0x80) == 0x80);
	}

	public void setInvalid(boolean value) {
		if (value)
			encodedValue[2] = (byte) (encodedValue[2] | 0x80);
		else
			encodedValue[2] = (byte) (encodedValue[2] & 0x7f);
	}

	/// <summary>
	/// Gets a value indicating whether this <see cref="lib60870.CP24Time2a"/> was
	/// substitued by an intermediate station
	/// </summary>
	/// <value><c>true</c> if substitued; otherwise, <c>false</c>.</value>
	public boolean isSubstitued() {
		return ((encodedValue[2] & 0x40) == 0x40);
	}

	public void setSubstitued(boolean value) {
		if (value)
			encodedValue[2] = (byte) (encodedValue[2] | 0x40);
		else
			encodedValue[2] = (byte) (encodedValue[2] & 0xbf);
	}

	public byte[] getEncodedValue() {
		return encodedValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("[CP24Time2a: Millisecond={0}, Second={1}, Minute={2}, Invalid={3}, Substitued={4}]",
				getMillisecond(), getSecond(), getMinute(), isInvalid(), isSubstitued());
	}

}