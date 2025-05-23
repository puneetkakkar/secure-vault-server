package com.securevault.main.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractBaseEntity {

	@NotBlank
	@Size(max = 20)
	@Email
	private String email;

	@NotBlank
	private String masterPasswordHash;

	@NotBlank
	private String masterPasswordHint;

	@NotBlank
	private String userKey;

	@NotBlank
	private String name;

	@Min(600000)
	@Positive
	private Integer kdfIterations;

	@DBRef
	private EmailVerificationToken emailVerificationToken;

	private LocalDateTime emailVerifiedAt;

	private int failedLoginAttempts;

	private LocalDateTime lockedUntil;

	@DBRef
	@Builder.Default
	private List<Role> roles = new ArrayList<>();

}
