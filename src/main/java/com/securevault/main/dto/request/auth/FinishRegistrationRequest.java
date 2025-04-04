package com.securevault.main.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class FinishRegistrationRequest {
	@NotBlank(message = "{not_blank}")
	@Email(message = "{invalid_email}")
	@Size(max = 100, message = "{max_length}")
	private String email;

	@NotBlank(message = "{not_blank}")
	private String masterPasswordHash;

	@NotBlank(message = "{not_blank}")
	private String masterPasswordHint;

	@Min(600000)
	@Positive(message = "{not_positive}")
	private Integer kdfIterations;

	@NotBlank(message = "{not_blank}")
	private String userKey;
}