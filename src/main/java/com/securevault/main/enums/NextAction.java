package com.securevault.main.enums;

import lombok.Getter;

@Getter
public enum NextAction {
	REDIRECT_TO_FINISH_REGISTRATION("redirect_to_finish_registration");

	private final String action;

	NextAction(String action) {
		this.action = action;
	}
}