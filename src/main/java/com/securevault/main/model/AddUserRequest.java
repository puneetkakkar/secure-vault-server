package com.securevault.main.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddUserRequest {
    @JsonProperty("name")
    public String name;
}
