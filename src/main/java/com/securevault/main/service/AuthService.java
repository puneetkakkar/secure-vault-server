package com.securevault.main.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
import com.securevault.main.util.Constants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
	@Value("${cookie.refresh.domain}")
	private String cookieDomain;

	@Value("${cookie.refresh.path}")
	private String cookiePath;

	@Value("${cookie.refresh.httpOnly}")
	private boolean isCookieHttpOnly;

	@Value("${cookie.refresh.secure}")
	private boolean isCookieSecure;

	@Value("${cookie.refresh.sameSite}")
	private String cookieSameSite;

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

	public void logout(User user, final String bearer) {
		JwtToken jwtToken = jwtTokenService.findByTokenOrRefreshToken(jwtTokenProvider.extractJwtFromBearerString(bearer));

		if (!user.getId().equals(jwtToken.getUserId())) {
			log.error("User id: {} is not equal to token user id: {}", user.getId(), jwtToken.getUserId());
		}

		jwtTokenService.delete(jwtToken);
	}

	public void logout(User user) {
		logout(user, httpServletRequest.getHeader(Constants.TOKEN_HEADER));
	}

	private TokenResponse refresh(final String refreshToken) {
		log.info("Refresh request received: {}", refreshToken);

		if (!jwtTokenProvider.validateToken(refreshToken)) {
			log.error("Refresh token is expired.");
			JwtToken refreshJwtToken = jwtTokenProvider.getTokenOrRefreshToken(refreshToken);

			if (refreshJwtToken != null) {
				jwtTokenService.delete(refreshJwtToken);
			}

			throw new RefreshTokenExpiredException(messageSourceService.get("access_denied"));
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

	private TokenResponse generateToken(final UUID userId, final Boolean rememberMe) {
		// Generate tokens
		String accessToken = jwtTokenProvider.generateJwt(userId.toString());
		String refreshToken = jwtTokenProvider.generateRefresh(userId.toString());

		if (rememberMe) {
			jwtTokenProvider.setRememberMe();
		}

		// Create and save JWT token
		JwtToken jwtToken = JwtToken.builder()
				.userId(userId)
				.token(accessToken)
				.refreshToken(refreshToken)
				.rememberMe(rememberMe)
				.ipAddress(httpServletRequest.getRemoteAddr())
				.userAgent(httpServletRequest.getHeader("User-Agent"))
				.tokenTimeToLive(jwtTokenProvider.getRefreshTokenExpiresIn())
				.build();

		jwtTokenService.save(jwtToken);

		log.info("Token generated for user: {}", userId);

		// Set refresh token cookie
		setRefreshTokenCookie(refreshToken);

		return TokenResponse.builder()
				.token(accessToken)
				.expiresIn(TokenExpiresInResponse.builder()
						.token(jwtTokenProvider.getTokenExpiresIn())
						.build())
				.build();

	}

	private void setRefreshTokenCookie(String refreshToken) {
		int maxAgeInSeconds = (int) Math.min(jwtTokenProvider.getRefreshTokenExpiresIn(), Integer.MAX_VALUE);

		ResponseCookie jwtCookie = ResponseCookie.from("refreshToken", refreshToken)
				.domain(cookieDomain)
				.path(cookiePath)
				.httpOnly(isCookieHttpOnly)
				.secure(isCookieSecure)
				.sameSite(cookieSameSite)
				.maxAge(maxAgeInSeconds)
				.build();

		httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
	}

}
