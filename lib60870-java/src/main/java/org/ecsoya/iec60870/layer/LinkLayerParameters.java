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
package org.ecsoya.iec60870.layer;

/// <summary>
/// Link layer specific parameters.
/// </summary>
public class LinkLayerParameters {
	private int addressLength = 1; /* 0/1/2 bytes */
	private int timeoutForACK = 1000; /* timeout for ACKs in ms */
	private long timeoutRepeat = 1000; /* timeout for repeating messages when no ACK received in ms */
	private boolean useSingleCharACK = true; /* use single char ACK for ACK (FC=0) or RESP_NO_USER_DATA (FC=9) */

	/**
	 * @return the addressLength
	 */
	public int getAddressLength() {
		return addressLength;
	}

	/**
	 * @return the timeoutForACK
	 */
	public int getTimeoutForACK() {
		return timeoutForACK;
	}

	/**
	 * @return the timeoutRepeat
	 */
	public long getTimeoutRepeat() {
		return timeoutRepeat;
	}

	/**
	 * @return the useSingleCharACK
	 */
	public boolean isUseSingleCharACK() {
		return useSingleCharACK;
	}

	/// <summary>
	/// Gets or sets the length of the link layer address field
	/// </summary>
	/// <para>The value can be either 0, 1, or 2 for balanced mode or 0, or 1 for
	/// unbalanced mode</para>
	/// <value>The length of the address in byte</value>
	/**
	 * @param addressLength the addressLength to set
	 */
	public void setAddressLength(int addressLength) {
		this.addressLength = addressLength;
	}

	/// <summary>
	/// Gets or sets the timeout for message ACK
	/// </summary>
	/// <value>The timeout to wait for message ACK in ms</value>
	/**
	 * @param timeoutForACK the timeoutForACK to set
	 */
	public void setTimeoutForACK(int timeoutForACK) {
		this.timeoutForACK = timeoutForACK;
	}

	/// <summary>
	/// Gets or sets the timeout for message repetition in case of missing ACK
	/// messages
	/// </summary>
	/// <value>The timeout for message repetition in ms</value>
	/**
	 * @param timeoutRepeat the timeoutRepeat to set
	 */
	public void setTimeoutRepeat(long timeoutRepeat) {
		this.timeoutRepeat = timeoutRepeat;
	}

	/// <summary>
	/// Gets or sets a value indicating whether the secondary link layer uses single
	/// character ACK instead of FC 0 or FC 9
	/// </summary>
	/// <value><c>true</c> if use single char ACK; otherwise, <c>false</c>.</value>
	/**
	 * @param useSingleCharACK the useSingleCharACK to set
	 */
	public void setUseSingleCharACK(boolean useSingleCharACK) {
		this.useSingleCharACK = useSingleCharACK;
	}
}
