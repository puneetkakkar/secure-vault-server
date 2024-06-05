
package com.securevault.main.exception;

import java.io.Serial;

/**
 * NotFoundException
 */
public class NotFoundException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public NotFoundException() {
		super("Not found!");
	}

	public NotFoundException(final String message) {
		super(message);
	}

}