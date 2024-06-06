package com.securevault.main.configuration;

import org.bson.UuidRepresentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoDBConfiguration {

	private final String databaseName = "vault-db";

	/**
	 * Build and set mongodb connection string
	 * 
	 * @return MongoClient connection string
	 */
	@Bean
	public MongoClient mongoClient() {
		String serverName = "localhost";
		String port = "27017";

		ConnectionString connectionString = new ConnectionString(
				"mongodb://" + serverName + ":" + port + "/" + databaseName);
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

	/**
	 * Applies mongo template to the mongo configuration
	 * 
	 * @return mongo template instance
	 */
	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongoClient(), databaseName);
	}

}
