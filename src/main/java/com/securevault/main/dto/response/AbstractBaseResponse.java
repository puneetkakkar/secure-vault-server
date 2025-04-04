package com.securevault.main.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@Getter
@Setter
public abstract class AbstractBaseResponse {
	protected String status;

	protected AbstractBaseResponse() {
	}
}
