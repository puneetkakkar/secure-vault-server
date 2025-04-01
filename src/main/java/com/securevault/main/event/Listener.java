package com.securevault.main.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
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
	@Async
	public void onUserEmailVerificationSendEvent(UserEmailVerificationSendEvent event) {
		try {
			log.info("[EmailService] Starting to send verification email to: {} ({})",
					event.getUser().getEmail(), event.getUser().getId());

			mailSenderService.sendUserEmailVerification(event.getUser());

			log.info("[EmailService] Successfully sent verification email to: {} ({})",
					event.getUser().getEmail(), event.getUser().getId());
		} catch (Exception e) {
			log.error("[EmailService] Failed to send verification email to: {} ({}). Error: {}",
					event.getUser().getEmail(), event.getUser().getId(), e.getMessage(), e);
			// In future, this is where we'd implement retry logic or queue to a dead letter
			// queue
		}
	}
}
