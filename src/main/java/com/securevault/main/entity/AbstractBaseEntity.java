package com.securevault.main.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

public abstract class AbstractBaseEntity implements Serializable {

    @Setter
    @Getter
    @Id
    private UUID id;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
