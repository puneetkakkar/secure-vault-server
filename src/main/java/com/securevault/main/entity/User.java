package com.securevault.main.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.securevault.main.model.AddUserRequest;

@Document(collection = "user")
public class User {

    @Id
    private String id;
    private String name;
    @SuppressWarnings("unused")
    private LocalDateTime createdOn;
    @SuppressWarnings("unused")
    private LocalDateTime updatedOn;

    public User(AddUserRequest request) {
        super();
        this.name = request.name;
        this.createdOn = LocalDateTime.now();
        this.updatedOn = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
