package com.securevault.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.core.SpringVersion;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableMongoRepositories("com.securevault.main.repository")
public class MainApplication {

	public static void main(String[] args) {
		System.out.println("Spring Version: " + SpringVersion.getVersion());
		SpringApplication.run(MainApplication.class, args);
	}

}
