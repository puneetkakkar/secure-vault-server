package com.securevault.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.core.SpringVersion;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableMongoRepositories("com.securevault.main.repository")
@RestController
public class MainApplication {

	public static void main(String[] args) {
		System.out.println("Spring Version: " + SpringVersion.getVersion());
		SpringApplication.run(MainApplication.class, args);
	}

	@GetMapping("/api/dummy")
	public String hello(@RequestParam(value = "name", defaultValue = "Java") String name) {
		return String.format("Yay! Hello %s!", name);
	}

}
