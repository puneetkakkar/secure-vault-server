package com.securevault.main.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.securevault.main.util.Constants;

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