package org.ecsoya.iec60870;

import java.time.LocalDateTime;

public class CP56Time2a {
	private byte[] encodedValue = new byte[7];

	public CP56Time2a(byte[] msg, int startIndex) throws ASDUParsingException {
		if (msg.length < startIndex + 7)
			throw new ASDUParsingException("Message too small for parsing CP56Time2a");

		for (int i = 0; i < 7; i++)
			encodedValue[i] = msg[startIndex + i];
	}

	public CP56Time2a(LocalDateTime time) {
		setMillisecond(time.getNano());
		setSecond(time.getSecond());
		setYear(time.getYear() % 100);
		setMonth(time.getMonthValue());
		setDayOfMonth(time.getDayOfMonth());
		setHour(time.getHour());
		setMinute(time.getMinute());
	}

	public CP56Time2a() {
		for (int i = 0; i < 7; i++)
			encodedValue[i] = 0;
	}

	/// <summary>
	/// Gets the date time.
	/// </summary>
	/// <returns>The date time.</returns>
	/// <param name="startYear">Start year.</param>
	public LocalDateTime getDateTime(int startYear) {
		int baseYear = (startYear / 100) * 100;

		if (this.getYear() < (startYear % 100))
			baseYear += 100;

		LocalDateTime value = LocalDateTime.of(baseYear + getYear(), getMonth(), getDayOfMonth(), getHour(),
				getMinute(), getSecond(), getMillisecond());

		return value;
	}

	public LocalDateTime getDateTime() {
		return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
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
	/// Gets or sets the hour (range 0 to 23)
	/// </summary>
	/// <value>The hour.</value>
	public int getHour() {
		return (encodedValue[3] & 0x1f);
	}

	public void setHour(int value) {
		encodedValue[3] = (byte) ((encodedValue[3] & 0xe0) | (value & 0x1f));
	}

	/// <summary>
	/// Gets or sets the day of week in range from 1 (Monday) until 7 (Sunday)
	/// </summary>
	/// <value>The day of week.</value>
	public int getDayOfWeek() {
		return ((encodedValue[4] & 0xe0) >> 5);
	}

	public void setDayOfWeek(int value) {
		encodedValue[4] = (byte) ((encodedValue[4] & 0x1f) | ((value & 0x07) << 5));
	}

	/// <summary>
	/// Gets or sets the day of month in range 1 to 31.
	/// </summary>
	/// <value>The day of month.</value>
	public int getDayOfMonth() {
		return (encodedValue[4] & 0x1f);
	}

	public void setDayOfMonth(int value) {
		encodedValue[4] = (byte) ((encodedValue[4] & 0xe0) + (value & 0x1f));
	}

	/// <summary>
	/// Gets the month in range from 1 (January) to 12 (December)
	/// </summary>
	/// <value>The month.</value>
	public int getMonth() {
		return (encodedValue[5] & 0x0f);
	}

	public void setMonth(int value) {
		encodedValue[5] = (byte) ((encodedValue[5] & 0xf0) + (value & 0x0f));
	}

	/// <summary>
	/// Gets the year in the range 0 to 99
	/// </summary>
	/// <value>The year.</value>
	public int getYear() {
		return (encodedValue[6] & 0x7f);
	}

	public void setYear(int value) {
		/* limit value to range 0 - 99 */
		value = value % 100;

		encodedValue[6] = (byte) ((encodedValue[6] & 0x80) + (value & 0x7f));
	}

	public boolean isSummerTime() {
		return ((encodedValue[3] & 0x80) != 0);
	}

	public void setSummerTime(boolean value) {
		if (value)
			encodedValue[3] |= 0x80;
		else
			encodedValue[3] &= 0x7f;
	}

	/// <summary>
	/// Gets a value indicating whether this <see cref="lib60870.CP56Time2a"/> is
	/// invalid.
	/// </summary>
	/// <value><c>true</c> if invalid; otherwise, <c>false</c>.</value>
	public boolean isInvalid() {
		return ((encodedValue[2] & 0x80) != 0);
	}

	public void setInvalid(boolean value) {
		if (value)
			encodedValue[2] |= 0x80;
		else
			encodedValue[2] &= 0x7f;
	}

	/// <summary>
	/// Gets a value indicating whether this <see cref="lib60870.CP26Time2a"/> was
	/// substitued by an intermediate station
	/// </summary>
	/// <value><c>true</c> if substitued; otherwise, <c>false</c>.</value>
	public boolean isSubstituted() {
		return ((encodedValue[2] & 0x40) == 0x40);
	}

	public void setSubstituted(boolean value) {
		if (value)
			encodedValue[2] |= 0x40;
		else
			encodedValue[2] &= 0xbf;
	}

	public byte[] getEncodedValue() {
		return encodedValue;
	}

	public String

			toString() {
		return String.format(
				"[CP56Time2a: Millisecond={0}, Second={1}, Minute={2}, Hour={3}, DayOfWeek={4}, DayOfMonth={5}, Month={6}, Year={7}, SummerTime={8}, Invalid={9} Substituted={10}]",
				getMillisecond(), getSecond(), getMinute(), getHour(), getDayOfWeek(), getDayOfMonth(), getMonth(),
				getYear(), isSummerTime(), isInvalid(), isSubstituted());
	}

}