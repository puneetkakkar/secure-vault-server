package com.securevault.main.exception;

public class UnverifiedEmailException extends RuntimeException {
	public UnverifiedEmailException(String message) {
		super(message);
	}
}