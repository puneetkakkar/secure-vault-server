package com.securevault.main.exception;

public class TokenExpiredException extends RuntimeException {
	public TokenExpiredException() {
		super("Token is expired!");
	}

	public TokenExpiredException(final String message) {
		super(message);
	}
}
