package com.securevault.main.configuration;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import com.securevault.main.event.UuidIdentifiedEntityEventListener;

@Configuration
public class AppConfiguration {

	@Bean
	public UuidIdentifiedEntityEventListener uuidIdentifiedEntityEventListener() {
		return new UuidIdentifiedEntityEventListener();
	}

	/**
	 * Simple application event multicaster bean.
	 *
	 * @return ApplicationEventMulticaster
	 */
	@Bean(name = "applicationEventMulticaster")
	public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
		SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
		eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());

		return eventMulticaster;
	}

	/**
	 * Spring template engine bean.
	 *
	 * @return SpringTemplateEngine
	 */
	@Bean
	public SpringTemplateEngine springTemplateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.addTemplateResolver(htmlTemplateResolver());

		return templateEngine;
	}

	/**
	 * Spring resource template resolver bean.
	 *
	 * @return SpringResourceTemplateResolver
	 */
	@Bean
	public SpringResourceTemplateResolver htmlTemplateResolver() {
		SpringResourceTemplateResolver emailTemplateResolver = new SpringResourceTemplateResolver();
		emailTemplateResolver.setPrefix("classpath:/templates/");
		emailTemplateResolver.setSuffix(".html");
		emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
		emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());

		return emailTemplateResolver;
	}

}
