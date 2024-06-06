package com.securevault.main.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.securevault.main.entity.User;
import com.securevault.main.service.JwtTokenService;
import com.securevault.main.service.UserService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

	public JwtTokenProvider(
			@Value("${app.secret}") final String appSecret,
			@Value("${app.jwt.token.expires-in}") final Long tokenExpiresIn,
			@Value("${app.jwt.refresh-token.expires-in}") final Long refreshTokenExpiresIn,
			@Value("${app.jwt.remember-me.expires-in}") final Long rememberMeTokenExpiresIn,
			final UserService userService,
			final JwtTokenService jwtTokenService) {
		this.userService = userService;
		this.appSecret = appSecret;
		this.tokenExpiresIn = tokenExpiresIn;
		this.refreshTokenExpiresIn = refreshTokenExpiresIn;
		this.rememberMeTokenExpiresIn = rememberMeTokenExpiresIn;
		this.jwtTokenService = jwtTokenService;
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

	public String generateJwt(final String id) {
		log.info("ID: {}", id);
		return generateTokenByUserId(id, tokenExpiresIn);
	}

	public String generateRefresh(final String id) {
		return generateTokenByUserId(id, refreshTokenExpiresIn);
	}

	public void setRememberMe() {
		this.refreshTokenExpiresIn = rememberMeTokenExpiresIn;
	}

	private Date getExpireDate(final Long expires) {
		return new Date(new Date().getTime() + expires);
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(appSecret.getBytes());
	}
}
