package com.securevault.main.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.securevault.main.service.MailSenderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class Listener {
	private final MailSenderService mailSenderService;

	@EventListener(UserEmailVerificationSendEvent.class)
	public void onUserEmailVerificationSendEvent(UserEmailVerificationSendEvent event) {
		log.info("[User e-mail verification mail send event listener] {} - {}",
				event.getUser().getEmail(), event.getUser().getId());
		mailSenderService.sendUserEmailVerification(event.getUser());
	}
}
