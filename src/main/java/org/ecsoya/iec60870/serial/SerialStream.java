/**
 * 
 */
package org.ecsoya.iec60870.serial;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public interface SerialStream extends Closeable, Flushable {

	int read(byte[] buffer, int offset, int length) throws IOException;

	// READ
	byte readByte() throws IOException;

	void setReadTimeout(int timeout);

	// WRITE

	void write(byte buffer[], int offset, int length) throws IOException;

	void write(int value) throws IOException;

}
