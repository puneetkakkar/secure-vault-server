package com.securevault.main.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.securevault.main.entity.JwtToken;
import com.securevault.main.entity.User;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.service.JwtTokenService;
import com.securevault.main.service.MessageSourceService;
import com.securevault.main.service.UserService;
import com.securevault.main.util.Constants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {
	private final UserService userService;
	private final String appSecret;

	@Getter
	private final Long tokenExpiresIn;
	private final MessageSourceService messageSourceService;

	@Getter
	private Long refreshTokenExpiresIn;

	private final Long rememberMeTokenExpiresIn;

	private final JwtTokenService jwtTokenService;
	private final HttpServletRequest httpServletRequest;

	public JwtTokenProvider(
			@Value("${app.jwt.secret}") final String appSecret,
			@Value("${app.jwt.token.expires-in}") final Long tokenExpiresIn,
			@Value("${app.jwt.refresh-token.expires-in}") final Long refreshTokenExpiresIn,
			@Value("${app.jwt.remember-me.expires-in}") final Long rememberMeTokenExpiresIn,
			final UserService userService,
			final JwtTokenService jwtTokenService,
			final HttpServletRequest httpServletRequest, MessageSourceService messageSourceService) {
		this.userService = userService;
		this.appSecret = appSecret;
		this.tokenExpiresIn = tokenExpiresIn;
		this.refreshTokenExpiresIn = refreshTokenExpiresIn;
		this.rememberMeTokenExpiresIn = rememberMeTokenExpiresIn;
		this.jwtTokenService = jwtTokenService;
		this.httpServletRequest = httpServletRequest;
		this.messageSourceService = messageSourceService;
	}

	public String generateTokenByUserId(final String id, final Long expires) {
		String token = Jwts.builder().subject(id).issuedAt(new Date()).expiration(getExpireDate(expires))
				.signWith(getSigningKey(), Jwts.SIG.HS256).compact();

		log.trace("Token is added to the local cache for userID: {}, ttl: {}", id, expires);

		return token;
	}

	public JwtUserDetails getPrincipal(final Authentication authentication) {
		return userService.getPrincipal(authentication);
	}

	public String getUserIdFromToken(final String token) {
		Claims claims = parseToken(token).getPayload();

		return claims.getSubject();
	}

	public User getUserFromToken(final String token) {
		try {
			return userService.findById(getUserIdFromToken(token));
		} catch (Exception e) {
			return null;
		}
	}

	public JwtToken getTokenOrRefreshToken(final String token) {
		try {
			return jwtTokenService.findByTokenOrRefreshToken(token);
		} catch (NotFoundException e) {
			log.error("[JWT] Token could not be found in Redis");
			return null;
		}
	}

	public String generateJwt(final String id) {
		return generateTokenByUserId(id, tokenExpiresIn);
	}

	public String generateRefresh(final String id) {
		return generateTokenByUserId(id, refreshTokenExpiresIn);
	}

	public TokenValidationResult validateToken(final String token) {
		try {
			// Parse and validate token format and signature
			Jws<Claims> claims = parseToken(token);

			// Check token expiration
			if (isTokenExpired(token)) {
				log.error("[JWT] Token expired: {}", token);
				return new TokenValidationResult(false, messageSourceService.get("token_expired"));
			}

			// Find token in database
			JwtToken jwtToken = getTokenOrRefreshToken(token);
			if (jwtToken == null) {
				log.error("[JWT] Token not found in database: {}", token);
				return new TokenValidationResult(false, messageSourceService.get("invalid_token"));
			}

			// Verify token belongs to user
			String userId = claims.getPayload().getSubject();
			if (!userId.equals(jwtToken.getUserId().toString())) {
				log.error("[JWT] Token user mismatch. Token user: {}, Expected user: {}", userId, jwtToken.getUserId());
				return new TokenValidationResult(false, messageSourceService.get("invalid_token"));
			}

			// Check user agent if it's an HTTP request
			if (httpServletRequest != null && !httpServletRequest.getHeader("User-agent").equals(jwtToken.getUserAgent())) {
				log.error("[JWT] User-agent mismatch. Request UA: {}, Token UA: {}",
						httpServletRequest.getHeader("User-agent"), jwtToken.getUserAgent());
				return new TokenValidationResult(false, messageSourceService.get("invalid_token"));
			}

			return new TokenValidationResult(true, null, jwtToken);
		} catch (UnsupportedJwtException e) {
			log.error("[JWT] Unsupported JWT token: {}", e.getMessage());
			return new TokenValidationResult(false, messageSourceService.get("invalid_token"));
		} catch (MalformedJwtException e) {
			log.error("[JWT] Malformed JWT token: {}", e.getMessage());
			return new TokenValidationResult(false, messageSourceService.get("invalid_token"));
		} catch (SignatureException e) {
			log.error("[JWT] Invalid JWT signature: {}", e.getMessage());
			return new TokenValidationResult(false, messageSourceService.get("invalid_token"));
		} catch (IllegalArgumentException e) {
			log.error("[JWT] Empty JWT claims: {}", e.getMessage());
			return new TokenValidationResult(false, messageSourceService.get("invalid_token"));
		} catch (Exception e) {
			log.error("[JWT] Unexpected error during token validation: {}", e.getMessage(), e);
			return new TokenValidationResult(false, messageSourceService.get("service_unavailable"));
		}
	}

	public void setRememberMe() {
		this.refreshTokenExpiresIn = rememberMeTokenExpiresIn;
	}

	public String extractJwtFromBearerString(final String bearer) {
		if (StringUtils.hasText(bearer) && bearer.startsWith(String.format("%s ", Constants.TOKEN_TYPE))) {
			return bearer.substring(Constants.TOKEN_TYPE.length() + 1);
		}

		return null;
	}

	public String extractJwtFromRequest(final HttpServletRequest request) {
		return extractJwtFromBearerString(request.getHeader(Constants.TOKEN_HEADER));
	}

	/**
	 * Parsing token
	 *
	 * @param token String jwt token to parse
	 * @return Jws claims object
	 */
	private Jws<Claims> parseToken(final String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
	}

	private boolean isTokenExpired(final String token) {
		return parseToken(token).getPayload().getExpiration().before(new Date());
	}

	private Date getExpireDate(final Long expires) {
		return new Date(new Date().getTime() + expires);
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(appSecret.getBytes());
	}

	// private void handleExpiredToken(ExpiredJwtException e, HttpServletRequest
	// httpServletRequest,
	// String accessDeniedMessage, String requestURI) {
	// if (requestURI.contains(ApiEndpoints.AUTH_REFRESH_TOKEN_URL)) {
	// log.info("[JWT Refresh] Expired JWT token detected. Token will be
	// refreshed.");
	// } else {
	// logErrorAndSetAttribute("Expired JWT token!", httpServletRequest,
	// accessDeniedMessage);
	// throw new AccessDeniedException(e.getMessage());
	// }
	// }

	// private void logAndThrowAccessDenied(String message, HttpServletRequest
	// httpServletRequest,
	// String accessDeniedMessage, Exception e) {
	// logErrorAndSetAttribute(message, httpServletRequest, accessDeniedMessage);
	// throw new AccessDeniedException(e.getMessage());
	// }

	// private void logErrorAndSetAttribute(String logMessage, HttpServletRequest
	// request, String message) {
	// log.error("[JWT] {}", logMessage);
	// request.setAttribute("access_denied", message);
	// }

	public static class TokenValidationResult {
		private final boolean valid;
		private final String errorMessage;
		private final JwtToken jwtToken;

		public TokenValidationResult(boolean valid, String errorMessage) {
			this(valid, errorMessage, null);
		}

		public TokenValidationResult(boolean valid, String errorMessage, JwtToken jwtToken) {
			this.valid = valid;
			this.errorMessage = errorMessage;
			this.jwtToken = jwtToken;
		}

		public boolean isValid() {
			return valid;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public JwtToken getJwtToken() {
			return jwtToken;
		}
	}
}
