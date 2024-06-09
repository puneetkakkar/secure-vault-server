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
import com.securevault.main.exception.RefreshTokenExpiredException;
import com.securevault.main.security.JwtTokenProvider;
import com.securevault.main.security.JwtUserDetails;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	private final HttpServletResponse httpServletResponse;
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

			UUID uuid = UUID.fromString(user.getId());

			return generateToken(uuid, rememberMe);
		} catch (NotFoundException e) {
			log.error("Authentication failed for email: {}", email);
			throw new AuthenticationCredentialsNotFoundException(badCredentialsMessage);
		}
	}

	public TokenResponse refreshFromCookie(final String refreshToken) {
		return refresh(refreshToken);
	}

	private TokenResponse refresh(final String refreshToken) {
		log.info("Refresh request received: {}", refreshToken);

		if (!jwtTokenProvider.validateToken(refreshToken)) {
			log.error("Refresh token is expired.");
			JwtToken refreshJwtToken = jwtTokenProvider.getTokenOrRefreshToken(refreshToken);

			if (refreshJwtToken != null) {
				jwtTokenService.delete(refreshJwtToken);
			}

			throw new RefreshTokenExpiredException();
		}

		User user = jwtTokenProvider.getUserFromToken(refreshToken);
		JwtToken oldToken = jwtTokenService.findByUserIdAndRefreshToken(user.getId(), refreshToken);
		if (oldToken != null && oldToken.getRememberMe()) {
			jwtTokenProvider.setRememberMe();
		}

		boolean rememberMe = false;
		if (oldToken != null) {
			rememberMe = oldToken.getRememberMe();
			jwtTokenService.delete(oldToken);
		}

		return generateToken(user.getId(), rememberMe);
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

		// Set the refresh token as an HttpOnly cookie
		// TODO: configure cookie secure flag to true and false based on
		// profiles/environment
		Cookie cookie = new Cookie("refreshToken", refreshToken);
		cookie.setDomain("localhost");
		cookie.setPath("/api/v1/auth");
		cookie.setHttpOnly(true);
		int maxAgeInSeconds = (int) Math.min(jwtTokenProvider.getRefreshTokenExpiresIn(), Integer.MAX_VALUE);
		cookie.setMaxAge(maxAgeInSeconds);
		httpServletResponse.addCookie(cookie);

		return TokenResponse.builder()
				.token(token)
				.expiresIn(
						TokenExpiresInResponse.builder()
								.token(jwtTokenProvider.getTokenExpiresIn())
								.build())
				.build();
	}

}
