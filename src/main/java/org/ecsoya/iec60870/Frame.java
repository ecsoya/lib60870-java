package org.ecsoya.iec60870;

/**
 * Abstract class to encode an application layer frame
 * 
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public interface Frame {
	public abstract void resetFrame();

	public abstract void setNextByte(byte value);

	public abstract void appendBytes(byte[] bytes);

	public abstract int getMsgSize();

	public abstract byte[] getBuffer();
}