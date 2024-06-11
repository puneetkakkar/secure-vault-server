package com.securevault.main.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.securevault.main.entity.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends MongoRepository<EmailVerificationToken, UUID> {
	Optional<EmailVerificationToken> findByUserId(UUID userId);

	Optional<EmailVerificationToken> findByToken(String token);

	@Query(value = "{'user.id': ?0}", delete = true)
	void deleteByUserId(UUID userId);
}
