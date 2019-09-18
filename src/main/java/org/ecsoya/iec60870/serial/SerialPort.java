/**
 * 
 */
package org.ecsoya.iec60870.serial;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public interface SerialPort {

	SerialStream getBaseStream();

	int getBaudRate();

	boolean isOpen();

	boolean open();

	/**
	 * Discard data from the serial driver's receive buffer.
	 */
	void discardInBuffer();

	void close();
}
