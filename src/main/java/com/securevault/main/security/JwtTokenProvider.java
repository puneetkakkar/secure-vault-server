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
import com.securevault.main.service.UserService;
import com.securevault.main.util.Constants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
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

	@Getter
	private Long refreshTokenExpiresIn;

	private final Long rememberMeTokenExpiresIn;

	private final JwtTokenService jwtTokenService;
	private final HttpServletRequest httpServletRequest;

	public JwtTokenProvider(
			@Value("${app.secret}") final String appSecret,
			@Value("${app.jwt.token.expires-in}") final Long tokenExpiresIn,
			@Value("${app.jwt.refresh-token.expires-in}") final Long refreshTokenExpiresIn,
			@Value("${app.jwt.remember-me.expires-in}") final Long rememberMeTokenExpiresIn,
			final UserService userService,
			final JwtTokenService jwtTokenService,
			final HttpServletRequest httpServletRequest) {
		this.userService = userService;
		this.appSecret = appSecret;
		this.tokenExpiresIn = tokenExpiresIn;
		this.refreshTokenExpiresIn = refreshTokenExpiresIn;
		this.rememberMeTokenExpiresIn = rememberMeTokenExpiresIn;
		this.jwtTokenService = jwtTokenService;
		this.httpServletRequest = httpServletRequest;
	}

	public String generateTokenByUserId(final String id, final Long expires) {
		String token = Jwts.builder().subject(id).issuedAt(new Date()).expiration(getExpireDate(expires))
				.signWith(getSigningKey(), Jwts.SIG.HS256).compact();

		log.trace("Token is added to the local cache for userID: {}, ttl: {}", id, expires);

		return token;
	}

	public User getPrincipal(final Authentication authentication) {
		return userService.getPrincipal(authentication);
	}

	public String getUserIdFromToken(final String token) {
		Claims claims = parseToken(token).getPayload();

		return claims.getSubject();
	}

	public String generateJwt(final String id) {
		log.info("ID: {}", id);
		return generateTokenByUserId(id, tokenExpiresIn);
	}

	public String generateRefresh(final String id) {
		return generateTokenByUserId(id, refreshTokenExpiresIn);
	}

	public boolean validateToken(final String token) {
		return validateToken(token, true);
	}

	public boolean validateToken(final String token, final boolean isHttp) {
		parseToken(token);
		try {
			JwtToken jwtToken = jwtTokenService.findByTokenOrRefreshToken(token);
			if (isHttp && !httpServletRequest.getHeader("User-agent").equals(jwtToken.getUserAgent())) {
				log.error("[JWT] User-agent is not matched");
				return false;
			}
		} catch (NotFoundException e) {
			log.error("[JWT] Token could not be found in Database");
			return false;
		}

		return !isTokenExpired(token);
	}

	public boolean validateToken(final String token, final HttpServletRequest httpServletRequest) {
		try {
			boolean isTokenValid = validateToken(token);
			if (!isTokenValid) {
				log.error("[JWT] Token could not be found in database");
				httpServletRequest.setAttribute("notfound", "Token is not found in cache");
			}
			return isTokenValid;
		} catch (UnsupportedJwtException e) {
			log.error("[JWT] Unsupported JWT token!");
			httpServletRequest.setAttribute("unsupported", "Unsupported JWT token!");
		} catch (MalformedJwtException e) {
			log.error("[JWT] Invalid JWT token!");
			httpServletRequest.setAttribute("invalid", "Invalid JWT token!");
		} catch (ExpiredJwtException e) {
			log.error("[JWT] Expired JWT token!");
			httpServletRequest.setAttribute("expired", "Expired JWT token");
		} catch (IllegalArgumentException e) {
			log.error("[JWT] Jwt claims string is empty");
			httpServletRequest.setAttribute("illegal", "JWT claims string is empty.");
		}

		return false;
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
}
