/**
 * 
 */
package org.ecsoya.iec60870.layer;

import java.io.IOException;

import org.ecsoya.iec60870.ConnectionException;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public abstract class PrimaryLinkLayer {

	public static class LinkLayerBusyException extends ConnectionException {
		public LinkLayerBusyException(String message) {
			super(message);
		}

		public LinkLayerBusyException(String message, Exception e) {
			super(message, e);
		}
	}

	public abstract void handleMessage(FunctionCodeSecondary fcs, boolean dir, boolean dfc, int address, byte[] msg,
			int userDataStart, int userDataLength) throws IOException, Exception;

	public abstract void runStateMachine() throws IOException;

	public abstract void sendLinkLayerTestFunction();

}
