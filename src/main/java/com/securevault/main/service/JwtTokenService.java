package com.securevault.main.service;

import org.springframework.stereotype.Service;

import com.securevault.main.entity.JwtToken;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.repository.JwtTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {
	private final JwtTokenRepository jwtTokenRepository;
	private final MessageSourceService messageSourceService;

	public JwtToken findByTokenOrRefreshToken(String token) {
		return jwtTokenRepository.findByTokenOrRefreshToken(token, token).orElseThrow(() -> new NotFoundException(
				messageSourceService.get("not_found_with_param", new String[] { messageSourceService.get("token") })));
	}

	/**
	 * Save a JWT token
	 * 
	 * @param jwtToken
	 */
	public void save(JwtToken jwtToken) {
		jwtTokenRepository.save(jwtToken);
	}

}
