package com.aegisql.id_builder;

public class IdSourceException extends RuntimeException {

	public IdSourceException(String string) {
		super(string);
	}

	public IdSourceException(String message, Throwable cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = 1L;

}
