package com.securevault.main.configuration.api;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApiVersionWebMvcRegistrations implements WebMvcRegistrations {

	@NonNull
	private ApiVersionProperties apiVersionProperties;

	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return new VersionedRequestMappingHandlerMapping(apiVersionProperties);
	}

}