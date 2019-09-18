/**
 * 
 */
package org.ecsoya.iec60870;

/**
 * @author Jin Liu (jin.liu@soyatec.com)
 */
public class ASDUParsingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ASDUParsingException() {
	}

	/**
	 * @param message
	 */
	public ASDUParsingException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ASDUParsingException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ASDUParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ASDUParsingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
