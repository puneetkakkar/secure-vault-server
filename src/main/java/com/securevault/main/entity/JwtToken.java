package com.securevault.main.entity;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Document(collection = "jwtTokens")
public class JwtToken {

	@Id
	private String id;

	private UUID userId;

	private String token;

	private String refreshToken;

	private Boolean rememberMe;

	private String ipAddress;

	private String userAgent;

	private Long tokenTimeToLive;
}
