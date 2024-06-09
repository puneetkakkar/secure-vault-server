package com.securevault.main.configuration.api;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "api")
public class ApiVersionProperties implements Serializable {

	private Type type = Type.URI;

	private String uriPrefix;

	public enum Type {
		URI,
	}

}
