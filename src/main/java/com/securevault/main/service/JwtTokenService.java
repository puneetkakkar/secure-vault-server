package com.securevault.main.service;

import org.springframework.stereotype.Service;

import com.securevault.main.entity.JwtToken;
import com.securevault.main.repository.JwtTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {
	private final JwtTokenRepository jwtTokenRepository;
	private final MessageSourceService messageSourceService;

	/**
	 * Save a JWT token
	 * 
	 * @param jwtToken
	 */
	public void save(JwtToken jwtToken) {
		jwtTokenRepository.save(jwtToken);
	}

}
