/**
 * 
 */
package org.ecsoya.iec60870.serial;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public interface SerialPort {

	void close();

	/**
	 * Discard data from the serial driver's receive buffer.
	 */
	void discardInBuffer();

	SerialStream getBaseStream();

	int getBaudRate();

	boolean isOpen();

	boolean open();
}
