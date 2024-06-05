package com.securevault.main.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public BadRequestException() {
		super("Bad request");
	}

	public BadRequestException(final String message) {
		super(message);
	}

}
