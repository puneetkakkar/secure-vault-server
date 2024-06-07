package com.securevault.main.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.securevault.main.event.UuidIdentifiedEntityEventListener;

@Configuration
public class AppConfiguration {

	@Bean
	public UuidIdentifiedEntityEventListener uuidIdentifiedEntityEventListener() {
		return new UuidIdentifiedEntityEventListener();
	}
}
