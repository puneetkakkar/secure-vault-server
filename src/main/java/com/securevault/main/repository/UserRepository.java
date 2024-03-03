package com.securevault.main.repository;

import com.securevault.main.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    @Query("{name:'?0'}")
    User getUserByName(String name);
}
