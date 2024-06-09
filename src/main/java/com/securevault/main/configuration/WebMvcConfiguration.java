package com.securevault.main.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.securevault.main.service.InterceptorService;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
	private final InterceptorService interceptorService;

	public WebMvcConfiguration(final InterceptorService interceptorService) {
		this.interceptorService = interceptorService;
	}

	@Override
	public void addInterceptors(@NonNull final InterceptorRegistry registry) {
		registry.addInterceptor(interceptorService).addPathPatterns("/api/**");
	}
}
