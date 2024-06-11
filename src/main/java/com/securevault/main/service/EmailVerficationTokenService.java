package com.securevault.main.service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.securevault.main.entity.EmailVerificationToken;
import com.securevault.main.entity.User;
import com.securevault.main.exception.BadRequestException;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.repository.EmailVerificationTokenRepository;
import com.securevault.main.util.Constants;
import com.securevault.main.util.RandomStringGenerator;

@Service
public class EmailVerficationTokenService {
	private final MessageSourceService messageSourceService;
	private final EmailVerificationTokenRepository emailVerificationTokenRepository;
	private final Long expiresIn;

	public EmailVerficationTokenService(EmailVerificationTokenRepository emailVerificationTokenRepository,
			MessageSourceService messageSourceService, @Value("${app.registration.email.token.expires-in}") Long expiresIn) {
		this.messageSourceService = messageSourceService;
		this.emailVerificationTokenRepository = emailVerificationTokenRepository;
		this.expiresIn = expiresIn;
	}

	public boolean isEmailVerificationTokenExpired(EmailVerificationToken token) {
		return token.getExpirationDate().before(new Date());
	}

	public EmailVerificationToken create(User user) {
		String newToken = new RandomStringGenerator(Constants.EMAIL_VERIFICATION_TOKEN_LENGTH).next();
		Date expirationDate = Date.from(Instant.now().plusSeconds(expiresIn));
		Optional<EmailVerificationToken> oldToken = emailVerificationTokenRepository.findByUserId(user.getId());
		EmailVerificationToken emailVerificationToken;

		if (oldToken.isPresent()) {
			emailVerificationToken = oldToken.get();
			emailVerificationToken.setToken(newToken);
			emailVerificationToken.setExpirationDate(expirationDate);
		} else {
			emailVerificationToken = EmailVerificationToken.builder().user(user).token(newToken)
					.expirationDate(expirationDate).build();
		}

		return emailVerificationTokenRepository.save(emailVerificationToken);
	}

	public User getUserByToken(String token) {
		EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.findByToken(token)
				.orElseThrow(() -> new NotFoundException(
						messageSourceService.get("not_found_with_param", new String[] { messageSourceService.get("token") })));

		if (isEmailVerificationTokenExpired(emailVerificationToken)) {
			throw new BadRequestException(
					messageSourceService.get("expired_with_param", new String[] { messageSourceService.get("token") }));
		}

		return emailVerificationToken.getUser();
	}

	public void deleteByUserId(UUID userId) {
		emailVerificationTokenRepository.deleteByUserId(userId);
	}
}
