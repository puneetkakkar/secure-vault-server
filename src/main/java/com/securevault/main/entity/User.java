package com.securevault.main.entity;

import com.securevault.main.model.AddUserRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user")
public class User {

    @Id
    private String id;
    private String name;
    private LocalDateTime createdOn;
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
