package com.securevault.main.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.securevault.main.entity.User;

import java.time.LocalDateTime;

public class UserResponse {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("created_on")
    private LocalDateTime createdOn;
    @JsonProperty("updated_on")
    private LocalDateTime updatedOn;

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
}
