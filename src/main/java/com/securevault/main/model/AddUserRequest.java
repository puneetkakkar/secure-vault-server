package com.securevault.main.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.securevault.main.entity.User;

public class AddUserRequest {
    @JsonProperty("name")
    public String name;
}
