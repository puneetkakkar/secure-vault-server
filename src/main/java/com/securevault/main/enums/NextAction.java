package com.securevault.main.enums;

import lombok.Getter;

@Getter
public enum NextAction {
	REDIRECT_TO_FINISH_REGISTRATION("redirect_to_finish_registration", "/finish-registration");

	private final String action;
	private final String redirectUrl;

	NextAction(String action, String redirectUrl) {
		this.action = action;
		this.redirectUrl = redirectUrl;
	}
}