package com.aegisql.id_builder;

/**
 * The Class IdSourceException.
 */
public class IdSourceException extends RuntimeException {

	/**
	 * Instantiates a new id source exception.
	 *
	 * @param string the string
	 */
	public IdSourceException(String string) {
		super(string);
	}

	/**
	 * Instantiates a new id source exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public IdSourceException(String message, Throwable cause) {
		super(message, cause);
	}

}
