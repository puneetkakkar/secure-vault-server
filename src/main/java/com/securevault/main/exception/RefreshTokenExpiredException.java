package com.securevault.main.exception;

public class RefreshTokenExpiredException extends TokenExpiredException {
	public RefreshTokenExpiredException() {
		super("Refresh token is expired!");
	}

	public RefreshTokenExpiredException(final String message) {
		super(message);
	}
}
