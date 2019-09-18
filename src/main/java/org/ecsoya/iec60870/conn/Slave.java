package org.ecsoya.iec60870.conn;

import org.ecsoya.iec60870.asdu.ASDUHandler;
import org.ecsoya.iec60870.asdu.ie.handler.ClockSynchronizationHandler;
import org.ecsoya.iec60870.asdu.ie.handler.CounterInterrogationHandler;
import org.ecsoya.iec60870.asdu.ie.handler.DelayAcquisitionHandler;
import org.ecsoya.iec60870.asdu.ie.handler.FileReadyHandler;
import org.ecsoya.iec60870.asdu.ie.handler.InterrogationHandler;
import org.ecsoya.iec60870.asdu.ie.handler.ReadHandler;
import org.ecsoya.iec60870.asdu.ie.handler.ResetProcessHandler;

public class Slave {

	protected boolean debugOutput;

	public final boolean getDebugOutput() {
		return this.debugOutput;
	}

	public final void setDebugOutput(boolean value) {
		debugOutput = value;
	}

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

	/**
	 * Sets a callback for interrogaton requests.
	 * 
	 * @param handler   The interrogation request handler callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void SetInterrogationHandler(InterrogationHandler handler, Object parameter) {
		this.interrogationHandler = handler;
		this.InterrogationHandlerParameter = parameter;
	}

	/**
	 * Sets a callback for counter interrogaton requests.
	 * 
	 * @param handler   The counter interrogation request handler callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void SetCounterInterrogationHandler(CounterInterrogationHandler handler, Object parameter) {
		this.counterInterrogationHandler = handler;
		this.counterInterrogationHandlerParameter = parameter;
	}

	/**
	 * Sets a callback for read requests.
	 * 
	 * @param handler   The read request handler callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void SetReadHandler(ReadHandler handler, Object parameter) {
		this.readHandler = handler;
		this.readHandlerParameter = parameter;
	}

	/**
	 * Sets a callback for the clock synchronization request.
	 * 
	 * @param handler   The clock synchronization request handler callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void SetClockSynchronizationHandler(ClockSynchronizationHandler handler, Object parameter) {
		this.clockSynchronizationHandler = handler;
		this.clockSynchronizationHandlerParameter = parameter;
	}

	public final void SetResetProcessHandler(ResetProcessHandler handler, Object parameter) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: this.resetProcessHandler = (Object parameter, IMasterConnection connection, ASDU asdu, byte qrp) -> handler.invoke(parameter, connection, asdu, qrp);
		this.resetProcessHandler = handler;
		this.resetProcessHandlerParameter = parameter;
	}

	public final void SetDelayAcquisitionHandler(DelayAcquisitionHandler handler, Object parameter) {
		this.delayAcquisitionHandler = handler;
		this.delayAcquisitionHandlerParameter = parameter;
	}

	/**
	 * Sets a callback to handle ASDUs (commands, requests) form clients. This
	 * callback can be used when no other callback handles the message from the
	 * client/master.
	 * 
	 * @param handler   The ASDU callback function
	 * @param parameter user provided parameter that is passed to the callback
	 */
	public final void SetASDUHandler(ASDUHandler handler, Object parameter) {
		this.asduHandler = handler;
		this.asduHandlerParameter = parameter;
	}

	public final void SetFileReadyHandler(FileReadyHandler handler, Object parameter) {
		fileReadyHandler = handler;
		fileReadyHandlerParameter = parameter;
	}

	protected FilesAvailable filesAvailable = new FilesAvailable();

	public final FilesAvailable GetAvailableFiles() {
		return filesAvailable;
	}
}