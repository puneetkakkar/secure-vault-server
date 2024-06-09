package com.securevault.main.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.securevault.main.configuration.api.ApiVersionProperties;
import com.securevault.main.configuration.api.ApiVersionWebMvcRegistrations;

@Configuration
@EnableConfigurationProperties(ApiVersionProperties.class)
public class ApiVersionAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ApiVersionWebMvcRegistrations apiVersionWebMvcRegistrations(ApiVersionProperties apiVersionProperties) {
		return new ApiVersionWebMvcRegistrations(apiVersionProperties);
	}
}
