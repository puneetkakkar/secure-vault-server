package com.securevault.main.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailVerificationRequest {
	@NotBlank(message = "{not_blank}")
	@Email(message = "{invalid_email}")
	@Size(max = 100, message = "{max_length}")
	private String email;

	@NotBlank(message = "{not_blank}")
	@Size(max = 100, message = "{max_length}")
	private String name;
}
