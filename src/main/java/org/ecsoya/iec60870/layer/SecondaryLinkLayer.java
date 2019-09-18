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

	public abstract void HandleMessage(FunctionCodePrimary fcp, boolean isBroadcast, int address, boolean fcb,
			boolean fcv, byte[] msg, int userDataStart, int userDataLength) throws IOException;

	public abstract void RunStateMachine();

	/**
	 * @return the address
	 */
	public int getAddress() {
		return Address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(int address) {
		Address = address;
	}

}
