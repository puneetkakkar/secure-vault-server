package com.securevault.main.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.securevault.main.dto.response.auth.TokenExpiresInResponse;
import com.securevault.main.dto.response.auth.TokenResponse;
import com.securevault.main.entity.JwtToken;
import com.securevault.main.entity.User;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.security.JwtTokenProvider;
import com.securevault.main.security.JwtUserDetails;
import com.securevault.main.util.UUIDUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final JwtTokenService jwtTokenService;
	private final JwtTokenProvider jwtTokenProvider;
	private final HttpServletRequest httpServletRequest;
	private final MessageSourceService messageSourceService;

	public TokenResponse login(String email, final String masterPasswordHash, final Boolean rememberMe) {
		log.info("login request received: {}", email);

		String badCredentialsMessage = messageSourceService.get("bad_credentials");

		try {
			User user = userService.findByEmail(email);
			email = user.getEmail();
		} catch (Exception e) {
			log.error("User not found with email: {}", email);
			throw new AuthenticationCredentialsNotFoundException(badCredentialsMessage);
		}

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email,
				masterPasswordHash);

		try {
			Authentication authentication = authenticationManager.authenticate(authenticationToken);
			JwtUserDetails user = jwtTokenProvider.getPrincipal(authentication);

			String uuidString = user.getId();
			UUID uuid = UUIDUtils.fromHexString(uuidString);

			return generateToken(uuid, rememberMe);
		} catch (NotFoundException e) {
			log.error("Authentication failed for email: {}", email);
			throw new AuthenticationCredentialsNotFoundException(badCredentialsMessage);
		}
	}

	private TokenResponse generateToken(final UUID id, final Boolean rememberMe) {
		String token = jwtTokenProvider.generateJwt(id.toString());
		String refreshToken = jwtTokenProvider.generateRefresh(id.toString());

		if (rememberMe) {
			jwtTokenProvider.setRememberMe();
		}

		jwtTokenService
				.save(JwtToken.builder().userId(id).token(token).refreshToken(refreshToken).rememberMe(rememberMe)
						.ipAddress(httpServletRequest.getRemoteAddr()).userAgent(httpServletRequest.getHeader("User-Agent"))
						.tokenTimeToLive(jwtTokenProvider.getRefreshTokenExpiresIn())
						.build());

		log.info("Token generated for user: {}", id);

		return TokenResponse.builder()
				.token(token)
				.refreshToken(refreshToken)
				.expiresIn(
						TokenExpiresInResponse.builder()
								.token(jwtTokenProvider.getTokenExpiresIn())
								.refreshToken(jwtTokenProvider.getRefreshTokenExpiresIn()).build())
				.build();
	}
}
