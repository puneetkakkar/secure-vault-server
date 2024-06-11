package com.securevault.main.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.securevault.main.entity.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends MongoRepository<EmailVerificationToken, String> {
	Optional<EmailVerificationToken> findByUserId(UUID userId);

	Optional<EmailVerificationToken> findByToken(String token);

	@Query("{'user.id': ?0}")
	void deleteByUserId(String userId);
}
