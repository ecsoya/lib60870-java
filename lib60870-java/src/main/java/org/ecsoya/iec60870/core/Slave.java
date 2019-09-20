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
package org.ecsoya.iec60870.core;

import java.util.function.Consumer;

import org.ecsoya.iec60870.core.file.FilesAvailable;
import org.ecsoya.iec60870.core.handler.ASDUHandler;
import org.ecsoya.iec60870.core.handler.ClockSynchronizationHandler;
import org.ecsoya.iec60870.core.handler.CounterInterrogationHandler;
import org.ecsoya.iec60870.core.handler.DelayAcquisitionHandler;
import org.ecsoya.iec60870.core.handler.FileReadyHandler;
import org.ecsoya.iec60870.core.handler.InterrogationHandler;
import org.ecsoya.iec60870.core.handler.ReadHandler;
import org.ecsoya.iec60870.core.handler.ResetProcessHandler;

public abstract class Slave implements IConnection {

	protected Consumer<String> debugOutput;

	public InterrogationHandler interrogationHandler = null;

	public Object InterrogationHandlerParameter = null;

	public CounterInterrogationHandler counterInterrogationHandler = null;
	public Object counterInterrogationHandlerParameter = null;

	public ReadHandler readHandler = null;
	public Object readHandlerParameter = null;

	public ClockSynchronizationHandler clockSynchronizationHandler = null;
	public Object clockSynchronizationHandlerParameter = null;

	public ResetProcessHandler resetProcessHandler = null;
	public Object resetProcessHandlerParameter = null;

	public DelayAcquisitionHandler delayAcquisitionHandler = null;
	public Object delayAcquisitionHandlerParameter = null;

	public ASDUHandler asduHandler = null;
	public Object asduHandlerParameter = null;

	protected FileReadyHandler fileReadyHandler = null;
	protected Object fileReadyHandlerParameter = null;

	protected FilesAvailable filesAvailable = new FilesAvailable();

	public final FilesAvailable getAvailableFiles() {
		return filesAvailable;
	}

	public final Consumer<String> getDebugOutput() {
		return this.debugOutput;
	}

	/**
	 * Sets a callback to handle ASDUs (commands, requests) form clients. This
	 * callback can be used when no other callback handles the message from the
	 * client/master.
	 *
	 * @param handler   The ASDU callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void setASDUHandler(ASDUHandler handler, Object parameter) {
		this.asduHandler = handler;
		this.asduHandlerParameter = parameter;
	}

	/**
	 * Sets a callback for the clock synchronization request.
	 *
	 * @param handler   The clock synchronization request handler callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void setClockSynchronizationHandler(ClockSynchronizationHandler handler, Object parameter) {
		this.clockSynchronizationHandler = handler;
		this.clockSynchronizationHandlerParameter = parameter;
	}

	/**
	 * Sets a callback for counter interrogaton requests.
	 *
	 * @param handler   The counter interrogation request handler callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void setCounterInterrogationHandler(CounterInterrogationHandler handler, Object parameter) {
		this.counterInterrogationHandler = handler;
		this.counterInterrogationHandlerParameter = parameter;
	}

	public final void setDebugOutput(Consumer<String> value) {
		debugOutput = value;
	}

	protected void debugLog(String msg) {
		if (debugOutput != null) {
			debugOutput.accept(msg);
		}
	}

	public final void setDelayAcquisitionHandler(DelayAcquisitionHandler handler, Object parameter) {
		this.delayAcquisitionHandler = handler;
		this.delayAcquisitionHandlerParameter = parameter;
	}

	public final void setFileReadyHandler(FileReadyHandler handler, Object parameter) {
		fileReadyHandler = handler;
		fileReadyHandlerParameter = parameter;
	}

	/**
	 * Sets a callback for interrogaton requests.
	 *
	 * @param handler   The interrogation request handler callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void setInterrogationHandler(InterrogationHandler handler, Object parameter) {
		this.interrogationHandler = handler;
		this.InterrogationHandlerParameter = parameter;
	}

	/**
	 * Sets a callback for read requests.
	 *
	 * @param handler   The read request handler callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void setReadHandler(ReadHandler handler, Object parameter) {
		this.readHandler = handler;
		this.readHandlerParameter = parameter;
	}

	public final void setResetProcessHandler(ResetProcessHandler handler, Object parameter) {
		this.resetProcessHandler = handler;
		this.resetProcessHandlerParameter = parameter;
	}

}
