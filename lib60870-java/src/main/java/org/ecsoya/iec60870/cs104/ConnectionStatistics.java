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
package org.ecsoya.iec60870.cs104;

/**
 * Provides some Connection statistics.
 */
public class ConnectionStatistics {

	private int sentMsgCounter = 0;
	private int rcvdMsgCounter = 0;
	private int rcvdTestFrActCounter = 0;
	private int rcvdTestFrConCounter = 0;

	/**
	 * Gets or sets the received message counter.
	 *
	 * <value>The received message counter.</value>
	 */
	public final int getRcvdMsgCounter() {
		return this.rcvdMsgCounter;
	}

	/**
	 * Counter for the TEST_FR_ACT messages received.
	 *
	 * <value>The TEST_FR_ACT counter.</value>
	 */
	public final int getRcvdTestFrActCounter() {
		return this.rcvdTestFrActCounter;
	}

	/**
	 * Counter for the TEST_FR_CON messages received.
	 *
	 * <value>The TEST_FR_CON counter.</value>
	 */
	public final int getRcvdTestFrConCounter() {
		return this.rcvdTestFrConCounter;
	}

	/**
	 * Gets or sets the sent message counter.
	 *
	 * <value>The sent message counter.</value>
	 */
	public final int getSentMsgCounter() {
		return this.sentMsgCounter;
	}

	public void increaseRcvdMsgCounter() {
		rcvdMsgCounter++;
	}

	public void increaseRcvdTestFrActCounter() {
		rcvdTestFrActCounter++;
	}

	public void increaseRcvdTestFrConCounter() {
		rcvdTestFrConCounter++;
	}

	public void increaseSentMsgCounter() {
		sentMsgCounter++;
	}

	public final void reset() {
		sentMsgCounter = 0;
		rcvdMsgCounter = 0;
		rcvdTestFrActCounter = 0;
		rcvdTestFrConCounter = 0;
	}

	public final void setRcvdMsgCounter(int value) {
		this.rcvdMsgCounter = value;
	}

	public final void setRcvdTestFrActCounter(int value) {
		this.rcvdTestFrActCounter = value;
	}

	public final void setRcvdTestFrConCounter(int value) {
		this.rcvdTestFrConCounter = value;
	}

	public final void setSentMsgCounter(int value) {
		this.sentMsgCounter = value;
	}

}
