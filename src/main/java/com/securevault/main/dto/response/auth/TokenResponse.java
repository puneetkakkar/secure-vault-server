package com.securevault.main.dto.response.auth;

import com.securevault.main.dto.response.AbstractBaseResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class TokenResponse extends AbstractBaseResponse {
	private String token;
	private String refreshToken;
	private TokenExpiresInResponse expiresIn;
}
