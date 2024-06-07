package com.securevault.main.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.securevault.main.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, UUID> {
	Optional<User> findByEmail(String email);

}
