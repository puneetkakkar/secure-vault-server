package com.securevault.main.exception;

public class TokenReuseException extends RuntimeException {
	public TokenReuseException(String message) {
		super(message);
	}
}