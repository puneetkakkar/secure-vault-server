package com.securevault.main.dto.response.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class TokenExpiresInResponse {
	private Long token;
}
