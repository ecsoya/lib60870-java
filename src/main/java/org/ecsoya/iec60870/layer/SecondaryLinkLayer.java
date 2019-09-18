/**
 * 
 */
package org.ecsoya.iec60870.layer;

import java.io.IOException;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public abstract class SecondaryLinkLayer {

	private int Address;

	/**
	 * @return the address
	 */
	public int getAddress() {
		return Address;
	}

	public abstract void handleMessage(FunctionCodePrimary fcp, boolean isBroadcast, int address, boolean fcb,
			boolean fcv, byte[] msg, int userDataStart, int userDataLength) throws IOException;

	public abstract void runStateMachine();

	/**
	 * @param address the address to set
	 */
	public void setAddress(int address) {
		Address = address;
	}

}
