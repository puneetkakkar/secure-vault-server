package com.securevault.main.entity;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@RedisHash(value = "jwtTokens")
public class JwtToken {

	@Id
	private UUID id;

	@Indexed
	private UUID userId;

	@Indexed
	private String token;

	@Indexed
	private String refreshToken;

	@Indexed
	private Boolean rememberMe;

	@Indexed
	private String ipAddress;

	@Indexed
	private String userAgent;

	@TimeToLive(unit = TimeUnit.MILLISECONDS)
	private Long tokenTimeToLive;

	public boolean isExpired() {
		return getExpirationDate().before(new Date());
	}

	public Date getExpirationDate() {
		return new Date(System.currentTimeMillis() + tokenTimeToLive);
	}
}
