package com.securevault.main.exception;

public class InvalidCredentialsException extends RuntimeException {
	public InvalidCredentialsException(String message) {
		super(message);
	}
}