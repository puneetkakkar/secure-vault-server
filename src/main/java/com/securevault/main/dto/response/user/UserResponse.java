package com.securevault.main.dto.response.user;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.securevault.main.entity.User;

public class UserResponse {

	@JsonProperty("id")
	private UUID id;
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

	public void setId(UUID id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
}
