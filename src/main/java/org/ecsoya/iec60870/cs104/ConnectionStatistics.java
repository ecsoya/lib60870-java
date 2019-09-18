package org.ecsoya.iec60870.cs104;

/**
 * Provides some Connection statistics.
 */
public class ConnectionStatistics {

	private int sentMsgCounter = 0;
	private int rcvdMsgCounter = 0;
	private int rcvdTestFrActCounter = 0;
	private int rcvdTestFrConCounter = 0;

	public final void Reset() {
		sentMsgCounter = 0;
		rcvdMsgCounter = 0;
		rcvdTestFrActCounter = 0;
		rcvdTestFrConCounter = 0;
	}

	/**
	 * Gets or sets the sent message counter.
	 * 
	 * <value>The sent message counter.</value>
	 */
	public final int getSentMsgCounter() {
		return this.sentMsgCounter;
	}

	public final void setSentMsgCounter(int value) {
		this.sentMsgCounter = value;
	}

	public void increaseSentMsgCounter() {
		sentMsgCounter++;
	}

	/**
	 * Gets or sets the received message counter.
	 * 
	 * <value>The received message counter.</value>
	 */
	public final int getRcvdMsgCounter() {
		return this.rcvdMsgCounter;
	}

	public final void setRcvdMsgCounter(int value) {
		this.rcvdMsgCounter = value;
	}

	public void increaseRcvdMsgCounter() {
		rcvdMsgCounter++;
	}

	/**
	 * Counter for the TEST_FR_ACT messages received.
	 * 
	 * <value>The TEST_FR_ACT counter.</value>
	 */
	public final int getRcvdTestFrActCounter() {
		return this.rcvdTestFrActCounter;
	}

	public final void setRcvdTestFrActCounter(int value) {
		this.rcvdTestFrActCounter = value;
	}

	public void increaseRcvdTestFrActCounter() {
		rcvdTestFrActCounter++;
	}

	/**
	 * Counter for the TEST_FR_CON messages received.
	 * 
	 * <value>The TEST_FR_CON counter.</value>
	 */
	public final int getRcvdTestFrConCounter() {
		return this.rcvdTestFrConCounter;
	}

	public final void setRcvdTestFrConCounter(int value) {
		this.rcvdTestFrConCounter = value;
	}

	public void increaseRcvdTestFrConCounter() {
		rcvdTestFrConCounter++;
	}

}