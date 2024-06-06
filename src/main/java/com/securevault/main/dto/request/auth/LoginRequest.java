package com.securevault.main.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
	@NotBlank(message = "{not_blank}")
	@Email(message = "{invalid_email}")
	private String email;

	@NotBlank(message = "{not_blank}")
	private String masterPasswordHash;

	private Boolean rememberMe;
}
