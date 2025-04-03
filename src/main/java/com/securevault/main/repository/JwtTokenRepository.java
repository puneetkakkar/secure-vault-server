package com.securevault.main.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.securevault.main.entity.JwtToken;

public interface JwtTokenRepository extends CrudRepository<JwtToken, UUID> {
	Optional<JwtToken> findByTokenOrRefreshToken(String token, String refreshToken);

	Optional<JwtToken> findByUserIdAndRefreshToken(UUID id, String refreshToken);

	void deleteAllByUserId(UUID userId);
}
