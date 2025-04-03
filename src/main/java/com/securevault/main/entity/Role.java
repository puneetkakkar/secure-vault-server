package com.securevault.main.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.securevault.main.util.Constants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AbstractBaseEntity {

	@Builder.Default
	@Field("users")
	private Set<String> users = new HashSet<>();

	@Field("name")
	private Constants.RoleEnum name;

	public Role(Constants.RoleEnum roleName) {
		this.name = roleName;
	}
}