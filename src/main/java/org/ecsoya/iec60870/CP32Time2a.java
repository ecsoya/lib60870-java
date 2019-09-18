/*******************************************************************************
 * Copyright (C) 2019 Ecsoya (jin.liu@soyatec.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.ecsoya.iec60870;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.ecsoya.iec60870.asdu.ASDUParsingException;

public class CP32Time2a {
	private byte[] encodedValue = new byte[4];

	public CP32Time2a() {
		for (int i = 0; i < 4; i++) {
			encodedValue[i] = 0;
		}
	}

	CP32Time2a(byte[] msg, int startIndex) throws ASDUParsingException {
		if (msg.length < startIndex + 4) {
			throw new ASDUParsingException("Message too small for parsing CP56Time2a");
		}

		for (int i = 0; i < 4; i++) {
			encodedValue[i] = msg[startIndex + i];
		}
	}

	public CP32Time2a(int hours, int minutes, int seconds, int milliseconds, boolean invalid, boolean summertime) {
		setHour(hours);
		setMinute(minutes);
		setSecond(seconds);
		setMillisecond(milliseconds);
		setInvalid(invalid);
		setSummerTime(summertime);
	}

	public CP32Time2a(LocalTime time) {
		setMillisecond(time.getNano());
		setSecond(time.getSecond());
		setHour(time.getHour());
		setMinute(time.getMinute());
	}

	public LocalDateTime getDateTime() {
		return LocalDateTime.now();
	}

	/// <summary>
	/// Gets the date time added to the reference day.
	/// </summary>
	/// <returns>The date time.</returns>
	/// <param name="refTime">Datetime representing the reference day</param>
	public LocalDateTime getDateTime(LocalDate refTime) {

		return LocalDateTime.of(refTime.getYear(), refTime.getMonth(), refTime.getDayOfMonth(), getHour(), getMinute(),
				getSecond(), getMillisecond());

	}

	public byte[] getEncodedValue() {
		return encodedValue;
	}

	/// <summary>
	/// Gets or sets the hour (range 0 to 23)
	/// </summary>
	/// <value>The hour.</value>
	public int getHour() {
		return (encodedValue[3] & 0x1f);
	}

	/// <summary>
	/// Gets or sets the millisecond part of the time value (range 0 to 999)
	/// </summary>
	/// <value>The millisecond.</value>
	public int getMillisecond() {
		return (encodedValue[0] + (encodedValue[1] * 0x100)) % 1000;
	}

	/// <summary>
	/// Gets or sets the minute (range 0 to 59)
	/// </summary>
	/// <value>The minute.</value>
	public int getMinute() {
		return (encodedValue[2] & 0x3f);
	}

	/// <summary>
	/// Gets or sets the second (range 0 to 59)
	/// </summary>
	/// <value>The second.</value>
	public int getSecond() {
		return (encodedValue[0] + (encodedValue[1] * 0x100)) / 1000;
	}

	/// <summary>
	/// Gets a value indicating whether this <see cref="lib60870.CP56Time2a"/> is
	/// invalid.
	/// </summary>
	/// <value><c>true</c> if invalid; otherwise, <c>false</c>.</value>
	public boolean isInvalid() {
		return ((encodedValue[2] & 0x80) != 0);
	}

	/// <summary>
	/// Gets a value indicating whether this <see cref="lib60870.CP26Time2a"/> was
	/// substitued by an intermediate station
	/// </summary>
	/// <value><c>true</c> if substitued; otherwise, <c>false</c>.</value>
	public boolean isSubstituted() {
		return ((encodedValue[2] & 0x40) == 0x40);
	}

	public boolean isSummerTime() {
		return ((encodedValue[3] & 0x80) != 0);
	}

	public void setHour(int value) {
		encodedValue[3] = (byte) ((encodedValue[3] & 0xe0) | (value & 0x1f));
	}

	public void setInvalid(boolean value) {
		if (value) {
			encodedValue[2] |= 0x80;
		} else {
			encodedValue[2] &= 0x7f;
		}

	}

	public void setMillisecond(int value) {
		int millies = (getSecond() * 1000) + value;

		encodedValue[0] = (byte) (millies & 0xff);
		encodedValue[1] = (byte) ((millies / 0x100) & 0xff);
	}

	public void setMinute(int value) {
		encodedValue[2] = (byte) ((encodedValue[2] & 0xc0) | (value & 0x3f));
	}

	public void setSecond(int value) {
		int millies = encodedValue[0] + (encodedValue[1] * 0x100);

		int msPart = millies % 1000;

		millies = (value * 1000) + msPart;

		encodedValue[0] = (byte) (millies & 0xff);
		encodedValue[1] = (byte) ((millies / 0x100) & 0xff);
	}

	public void setSubstitued(boolean value) {
		if (value) {
			encodedValue[2] |= 0x40;
		} else {
			encodedValue[2] &= 0xbf;
		}
	}

	public void setSummerTime(boolean value) {
		if (value) {
			encodedValue[3] |= 0x80;
		} else {
			encodedValue[3] &= 0x7f;
		}
	}

	@Override
	public String

			toString() {
		return String.format(
				"[CP32Time2a: Millisecond={0}, Second={1}, Minute={2}, Hour={3}, SummerTime={4}, Invalid={5} Substituted={6}]",
				getMillisecond(), getSecond(), getMinute(), getHour(), isSummerTime(), isInvalid(), isSubstituted());
	}

}
