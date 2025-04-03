package com.securevault.main.exception;

public class AccountLockedException extends RuntimeException {
	public AccountLockedException(String message) {
		super(message);
	}
}