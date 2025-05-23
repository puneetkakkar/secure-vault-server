package com.securevault.main.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.securevault.main.entity.JwtToken;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.repository.JwtTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {
	private final JwtTokenRepository jwtTokenRepository;
	private final MessageSourceService messageSourceService;

	public JwtToken findByTokenOrRefreshToken(String token) {
		return jwtTokenRepository.findByTokenOrRefreshToken(token, token).orElseThrow(() -> new NotFoundException(
				messageSourceService.get("not_found_with_param", new String[] { messageSourceService.get("token") })));
	}

	public Optional<JwtToken> tryFindByTokenOrRefreshToken(String token) {
		return jwtTokenRepository.findByTokenOrRefreshToken(token, token);
	}

	public JwtToken findByUserIdAndRefreshToken(UUID id, String refreshToken) {
		return jwtTokenRepository.findByUserIdAndRefreshToken(id, refreshToken).orElseThrow(() -> new NotFoundException(
				messageSourceService.get("not_found_with_param", new String[] { messageSourceService.get("token") })));
	}

	public boolean isValid(String token) {
		return jwtTokenRepository.findByTokenOrRefreshToken(token, token)
				.map(jwtToken -> !jwtToken.isExpired())
				.orElse(false);
	}

	public void save(JwtToken jwtToken) {
		jwtTokenRepository.save(jwtToken);
		log.info("Saved token for user: {}", jwtToken.getUserId());
	}

	public void delete(JwtToken jwtToken) {
		jwtTokenRepository.delete(jwtToken);
		log.info("Deleted token: {}", jwtToken);
	}

	public void deleteAllByUserId(UUID userId) {
		log.info("Deleting all tokens for user: {}", userId);
		jwtTokenRepository.deleteAllByUserId(userId);
	}

}
