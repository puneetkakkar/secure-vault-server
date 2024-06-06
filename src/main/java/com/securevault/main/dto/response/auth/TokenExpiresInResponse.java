package com.securevault.main.dto.response.auth;

import com.securevault.main.dto.response.AbstractBaseResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class TokenExpiresInResponse extends AbstractBaseResponse {
	private Long token;
	private Long refreshToken;
}
