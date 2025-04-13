package com.securevault.main.dto.response.auth;

import com.securevault.main.dto.response.SuccessResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class TokenResponse extends SuccessResponse {
	private String token;
	private TokenExpiresInResponse expiresIn;
}
