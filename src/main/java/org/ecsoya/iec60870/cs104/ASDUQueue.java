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

import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

import org.ecsoya.iec60870.BufferFrame;
import org.ecsoya.iec60870.asdu.ASDU;
import org.ecsoya.iec60870.asdu.ApplicationLayerParameters;
import org.ecsoya.iec60870.tangible.OutObject;

public class ASDUQueue {
	private static class ASDUQueueEntry {
		public long entryTimestamp;
		public BufferFrame asdu;
		public QueueEntryState state;
	}

	private enum QueueEntryState {
		NOT_USED,
		WAITING_FOR_TRANSMISSION,
		SENT_BUT_NOT_CONFIRMED
	}

	// Queue for messages (ASDUs)
	private ASDUQueueEntry[] enqueuedASDUs = null;
	private int oldestQueueEntry = -1;
	private int latestQueueEntry = -1;
	private int numberOfAsduInQueue = 0;
	private int maxQueueSize;

	private ApplicationLayerParameters parameters;

	private Consumer<String> debugLog = null;

	public ASDUQueue(int maxQueueSize, ApplicationLayerParameters parameters, Consumer<String> DebugLog) {
		enqueuedASDUs = new ASDUQueueEntry[maxQueueSize];

		for (int i = 0; i < maxQueueSize; i++) {
			enqueuedASDUs[i].asdu = new BufferFrame(new byte[260], 6);
			enqueuedASDUs[i].state = QueueEntryState.NOT_USED;
		}

		this.oldestQueueEntry = -1;
		this.latestQueueEntry = -1;
		this.numberOfAsduInQueue = 0;
		this.maxQueueSize = maxQueueSize;
		this.parameters = parameters;
		this.debugLog = DebugLog;
	}

	private void debugLog(String msg) {
		if (debugLog != null) {
			debugLog.accept(msg);
		}
	}

	public void enqueueAsdu(ASDU asdu) {
		synchronized (enqueuedASDUs) {

			if (oldestQueueEntry == -1) {
				oldestQueueEntry = 0;
				latestQueueEntry = 0;
				numberOfAsduInQueue = 1;

				enqueuedASDUs[0].asdu.resetFrame();
				asdu.encode(enqueuedASDUs[0].asdu, parameters);

				enqueuedASDUs[0].entryTimestamp = System.currentTimeMillis();
				enqueuedASDUs[0].state = QueueEntryState.WAITING_FOR_TRANSMISSION;
			} else {
				latestQueueEntry = (latestQueueEntry + 1) % maxQueueSize;

				if (latestQueueEntry == oldestQueueEntry) {
					oldestQueueEntry = (oldestQueueEntry + 1) % maxQueueSize;
				} else {
					numberOfAsduInQueue++;
				}

				enqueuedASDUs[latestQueueEntry].asdu.resetFrame();
				asdu.encode(enqueuedASDUs[latestQueueEntry].asdu, parameters);

				enqueuedASDUs[latestQueueEntry].entryTimestamp = System.currentTimeMillis();
				enqueuedASDUs[latestQueueEntry].state = QueueEntryState.WAITING_FOR_TRANSMISSION;
			}
		}

		debugLog("Queue contains " + numberOfAsduInQueue + " messages (oldest: " + oldestQueueEntry + " latest: "
				+ latestQueueEntry + ")");
	}

	public BufferFrame getNextWaitingASDU(OutObject<Long> timestampWrapper, OutObject<Integer> indexWrapper) {
		long timestamp = 0;
		int index = -1;

		if (enqueuedASDUs == null) {
			return null;
		}

		// synchronized (enqueuedASDUs) {
		if (numberOfAsduInQueue > 0) {

			int currentIndex = oldestQueueEntry;

			while (enqueuedASDUs[currentIndex].state != QueueEntryState.WAITING_FOR_TRANSMISSION) {

				if (enqueuedASDUs[currentIndex].state == QueueEntryState.NOT_USED) {
					break;
				}

				currentIndex = (currentIndex + 1) % maxQueueSize;

				// break if we reached the oldest entry again
				if (currentIndex == oldestQueueEntry) {
					break;
				}
			}

			if (enqueuedASDUs[currentIndex].state == QueueEntryState.WAITING_FOR_TRANSMISSION) {
				enqueuedASDUs[currentIndex].state = QueueEntryState.SENT_BUT_NOT_CONFIRMED;
				timestamp = enqueuedASDUs[currentIndex].entryTimestamp;
				index = currentIndex;

				timestampWrapper.argValue = timestamp;
				indexWrapper.argValue = index;

				return enqueuedASDUs[currentIndex].asdu;
			}

			return null;
		}
		// }

		return null;
	}

	public void lockASDUQueue() {
//		Monitor.Enter(enqueuedASDUs);
		LockSupport.park(enqueuedASDUs);
	}

	public void markASDUAsConfirmed(int index, long timestamp) {
		if (enqueuedASDUs == null) {
			return;
		}

		if ((index < 0) || (index > enqueuedASDUs.length)) {
			return;
		}

		synchronized (enqueuedASDUs) {

			if (numberOfAsduInQueue > 0) {

				if (enqueuedASDUs[index].state == QueueEntryState.SENT_BUT_NOT_CONFIRMED) {

					if (enqueuedASDUs[index].entryTimestamp == timestamp) {

						int currentIndex = index;

						while (enqueuedASDUs[currentIndex].state == QueueEntryState.SENT_BUT_NOT_CONFIRMED) {

							debugLog("Remove from queue with index " + currentIndex);

							enqueuedASDUs[currentIndex].state = QueueEntryState.NOT_USED;
							enqueuedASDUs[currentIndex].entryTimestamp = 0;
							numberOfAsduInQueue -= 1;

							if (numberOfAsduInQueue == 0) {
								oldestQueueEntry = -1;
								latestQueueEntry = -1;
								break;
							}

							if (currentIndex == oldestQueueEntry) {
								oldestQueueEntry = (index + 1) % maxQueueSize;

								if (numberOfAsduInQueue == 1) {
									latestQueueEntry = oldestQueueEntry;
								}

								break;
							}

							currentIndex = currentIndex - 1;

							if (currentIndex < 0) {
								currentIndex = maxQueueSize - 1;
							}

							// break if we reached the first deleted entry again
							if (currentIndex == index) {
								break;
							}

						}

						debugLog("queue state: noASDUs: " + numberOfAsduInQueue + " oldest: " + oldestQueueEntry
								+ " latest: " + latestQueueEntry);
					}
				}
			}
		}
	}

	public void unlockASDUQueue() {
//		Monitor.Exit(enqueuedASDUs);
//		LockSupport.unpark(thread);
	}

	public void unmarkAllASDUs() {
		synchronized (enqueuedASDUs) {
			if (numberOfAsduInQueue > 0) {
				for (int i = 0; i < enqueuedASDUs.length; i++) {
					if (enqueuedASDUs[i].state == QueueEntryState.SENT_BUT_NOT_CONFIRMED) {
						enqueuedASDUs[i].state = QueueEntryState.WAITING_FOR_TRANSMISSION;
					}
				}
			}
		}
	}
}
