package com.securevault.main.entity;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractUuidIdentified {

	protected UUID id;

}
