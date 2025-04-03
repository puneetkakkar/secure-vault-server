package com.securevault.main.entity;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "email_verification_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken extends AbstractBaseEntity {
	@DBRef
	private User user;

	private String token;

	private Date expirationDate;

	public boolean isExpired() {
		return expirationDate.before(new Date());
	}
}
