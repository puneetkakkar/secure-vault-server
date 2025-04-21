package com.securevault.main.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.securevault.main.enums.NextAction;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private NextAction nextAction;
	private String redirectUrl;

	public BadRequestException() {
	}

	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException withNextAction(NextAction nextAction) {
		this.nextAction = nextAction;
		this.redirectUrl = nextAction.getRedirectUrl();
		return this;
	}

	public NextAction getNextAction() {
		return nextAction;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

}
