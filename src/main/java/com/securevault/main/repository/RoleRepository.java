package com.securevault.main.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.securevault.main.entity.Role;
import com.securevault.main.util.Constants;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
  Optional<Role> findByName(Constants.RoleEnum name);
}
