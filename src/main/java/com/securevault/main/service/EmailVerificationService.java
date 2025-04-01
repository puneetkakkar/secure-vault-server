package com.securevault.main.service;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.main.dto.request.auth.SendEmailVerificationRequest;
import com.securevault.main.entity.EmailVerificationToken;
import com.securevault.main.entity.User;
import com.securevault.main.event.UserEmailVerificationSendEvent;
import com.securevault.main.exception.BadRequestException;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.repository.EmailVerificationTokenRepository;
import com.securevault.main.util.Constants;
import com.securevault.main.util.RandomStringGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmailVerificationService {
	private final UserService userService;
	private final EmailVerificationTokenRepository tokenRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final MessageSourceService messageSourceService;

	@Value("${app.registration.email.token.expires-in}")
	private Long tokenExpiresIn;

	public void sendEmailVerification(SendEmailVerificationRequest request) {
		log.info("Sending email verification to email: {}", request.getEmail());

		User user = userService.getOrCreateUnverifiedUser(request.getEmail(), request.getName());

		if (user.getEmailVerifiedAt() != null) {
			throw new BadRequestException(messageSourceService.get("email_already_verified"));
		}

		sendVerificationEmail(user);
	}

	public void verifyEmail(String token) {
		log.info("Verifying email with token: {}", token);

		EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new NotFoundException(
						messageSourceService.get("not_found_with_param",
								new String[] { messageSourceService.get("token") })));

		if (verificationToken.isExpired()) {
			throw new BadRequestException(
					messageSourceService.get("expired_with_param",
							new String[] { messageSourceService.get("token") }));
		}

		User user = verificationToken.getUser();

		if (user.getEmailVerifiedAt() != null) {
			throw new BadRequestException(messageSourceService.get("email_already_verified"));
		}

		userService.markEmailAsVerified(user);
		tokenRepository.delete(verificationToken);

		log.info("Email verified for user: {}", user.getEmail());
	}

	private void sendVerificationEmail(User user) {
		String tokenValue = new RandomStringGenerator(Constants.EMAIL_VERIFICATION_TOKEN_LENGTH).next();
		Date expirationDate = Date.from(Instant.now().plusSeconds(tokenExpiresIn));

		// Delete existing token if any
		tokenRepository.deleteByUserId(user.getId());

		// Create new token
		EmailVerificationToken token = EmailVerificationToken.builder()
				.user(user)
				.token(tokenValue)
				.expirationDate(expirationDate)
				.build();

		// Save token and update user
		token = tokenRepository.save(token);
		user.setEmailVerificationToken(token);

		// Publish email event
		eventPublisher.publishEvent(new UserEmailVerificationSendEvent(this, user));
		log.info("Verification email sent to: {}", user.getEmail());
	}
}
