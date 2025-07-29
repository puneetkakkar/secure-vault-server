package com.securevault.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.core.SpringVersion;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableMongoRepositories("com.securevault.main.repository")
@EnableScheduling
public class MainApplication {

	public static void main(String[] args) {
		System.out.println("Spring Version: " + SpringVersion.getVersion());
		log.info("🚀 Starting Secure Vault application...");
		SpringApplication.run(MainApplication.class, args);
	}

}
