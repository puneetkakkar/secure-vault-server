package com.securevault.main.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.securevault.main.dto.response.auth.TokenExpiresInResponse;
import com.securevault.main.dto.response.auth.TokenResponse;
import com.securevault.main.entity.JwtToken;
import com.securevault.main.entity.User;
import com.securevault.main.exception.AccountLockedException;
import com.securevault.main.exception.BadRequestException;
import com.securevault.main.exception.InvalidCredentialsException;
import com.securevault.main.exception.InvalidTokenException;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.exception.TokenReuseException;
import com.securevault.main.exception.UnverifiedEmailException;
import com.securevault.main.security.JwtTokenProvider;
import com.securevault.main.security.JwtUserDetails;

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
		log.info("Login request received for email: {}", email);

		try {
			// Find user
			User user = userService.findByEmail(email);

			// Check if user is properly registered
			if (user.getMasterPasswordHash() == null) {
				log.error("Login attempt for unregistered user: {}", email);
				throw new BadRequestException(messageSourceService.get("user_not_registered"));
			}

			// Check if email is verified
			if (user.getEmailVerifiedAt() == null) {
				log.error("Login attempt for unverified email: {}", email);
				throw new UnverifiedEmailException(messageSourceService.get("email_not_verified"));
			}

			// Check if user is locked
			if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
				log.error("Login attempt for locked user: {}", email);
				throw new AccountLockedException(messageSourceService.get("account_locked"));
			}

			// Authenticate user
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email,
					masterPasswordHash);

			try {
				Authentication authentication = authenticationManager.authenticate(authenticationToken);
				JwtUserDetails userDetails = jwtTokenProvider.getPrincipal(authentication);

				// Reset failed login attempts on successful login
				userService.resetFailedLoginAttempts(email);

				return generateAndStoreTokens(UUID.fromString(userDetails.getId()), rememberMe);
			} catch (AuthenticationException e) {
				// Increment failed login attempts
				userService.incrementFailedLoginAttempts(email);

				log.error("Invalid credentials for user: {}", email);
				throw new InvalidCredentialsException(messageSourceService.get("invalid_credentials"));
			}
		} catch (NotFoundException e) {
			log.error("User not found with email: {}", email);
			throw new InvalidCredentialsException(messageSourceService.get("invalid_credentials"));
		}
	}

	public TokenResponse refreshFromCookie(final String refreshToken) {
		try {
			// Validate refresh token and get validation result
			JwtTokenProvider.TokenValidationResult validationResult = jwtTokenProvider.validateToken(refreshToken);
			if (!validationResult.isValid()) {
				log.error("Invalid refresh token: {}", validationResult.getErrorMessage());
				throw new InvalidTokenException(validationResult.getErrorMessage());
			}

			// Get the validated token from the result
			JwtToken existingToken = validationResult.getJwtToken();
			if (existingToken == null) {
				log.error("Token record not found in validation result");
				throw new InvalidTokenException(messageSourceService.get("invalid_refresh_token"));
			}

			// Get user from the validated token
			User user = userService.findById(existingToken.getUserId());

			// Check if user exists and is not locked
			if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
				log.error("Account locked for user: {}", user.getEmail());
				throw new AccountLockedException(messageSourceService.get("account_locked"));
			}

			// Check if refresh token was already used
			if (existingToken.getRefreshTokenUsedAt() != null) {
				log.error("Refresh token reuse detected for user: {}", user.getEmail());
				// Invalidate all user's tokens as a security measure
				jwtTokenService.deleteAllByUserId(user.getId());
				throw new TokenReuseException(messageSourceService.get("token_reuse_detected"));
			}

			// Mark refresh token as used
			existingToken.setRefreshTokenUsedAt(LocalDateTime.now());
			jwtTokenService.save(existingToken);

			// Generate and store new tokens
			TokenResponse newTokens = generateAndStoreTokens(user.getId(), existingToken.getRememberMe());

			// Delete old token
			jwtTokenService.delete(existingToken);

			return newTokens;
		} catch (NotFoundException e) {
			log.error("User not found during token refresh");
			throw new InvalidTokenException(messageSourceService.get("invalid_refresh_token"));
		} catch (TokenReuseException e) {
			log.error("Token reuse attempt detected");
			throw new TokenReuseException(messageSourceService.get("token_reuse_detected"));
		}
	}

	public void logout(User user, final String token) {
		log.info("Logout request received for user: {}", user.getEmail());

		// Find token in database
		JwtToken jwtToken = jwtTokenService.findByTokenOrRefreshToken(token);
		if (jwtToken == null) {
			log.error("Token not found in database");
			throw new InvalidTokenException(messageSourceService.get("invalid_token"));
		}

		// Verify token belongs to user
		if (!user.getId().equals(jwtToken.getUserId())) {
			log.error("Token user mismatch. Expected: {}, Actual: {}", user.getId(), jwtToken.getUserId());
			throw new InvalidTokenException(messageSourceService.get("invalid_token"));
		}

		// Delete the token
		jwtTokenService.delete(jwtToken);

		// Clear refresh token cookie
		clearRefreshTokenCookie();

		log.info("User {} successfully logged out", user.getEmail());
	}

	private TokenResponse generateAndStoreTokens(final UUID userId, final Boolean rememberMe) {
		// Set remember me if needed
		if (rememberMe) {
			jwtTokenProvider.setRememberMe();
		}

		// Generate tokens
		String accessToken = jwtTokenProvider.generateJwt(userId.toString());
		String refreshToken = jwtTokenProvider.generateRefresh(userId.toString());

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

		log.info("Tokens generated for user: {}", userId);

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

		ResponseCookie jwtCookie = ResponseCookie.from("sv.rftkn", refreshToken)
				.domain(cookieDomain)
				.path(cookiePath)
				.httpOnly(isCookieHttpOnly)
				.secure(isCookieSecure)
				.sameSite(cookieSameSite)
				.maxAge(maxAgeInSeconds)
				.build();

		httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
	}

	private void clearRefreshTokenCookie() {
		ResponseCookie jwtCookie = ResponseCookie.from("sv.rftkn", "")
				.domain(cookieDomain)
				.path(cookiePath)
				.httpOnly(isCookieHttpOnly)
				.secure(isCookieSecure)
				.sameSite(cookieSameSite)
				.maxAge(0)
				.build();

		httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
	}

}
