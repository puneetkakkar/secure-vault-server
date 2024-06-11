package com.securevault.main.event;

import org.springframework.context.ApplicationEvent;

import com.securevault.main.entity.User;

import lombok.Getter;

@Getter
public class UserEmailVerificationSendEvent extends ApplicationEvent {
	private final User user;

	public UserEmailVerificationSendEvent(Object source, User user) {
		super(source);
		this.user = user;
	}
}
