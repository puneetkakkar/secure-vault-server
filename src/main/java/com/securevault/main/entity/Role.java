package com.securevault.main.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import com.securevault.main.util.Constants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "roles")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AbstractBaseEntity {

	@Builder.Default
	private Set<String> users = new HashSet<>();

	private Constants.RoleEnum name;

	public Role(Constants.RoleEnum roleName) {
		this.name = roleName;
	}
}