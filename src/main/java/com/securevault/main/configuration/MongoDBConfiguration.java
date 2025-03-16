package com.securevault.main.configuration;

import com.securevault.main.entity.AbstractBaseEntity;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;

import java.util.UUID;

@Configuration
public class MongoDBConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String connectionUri;

    @Value("${spring.data.mongodb.database}")
    private String database;

    /**
     * Build and set mongodb connection string
     *
     * @return MongoClient connection string
     */
    @Bean
    public MongoClient mongoClient() {

        ConnectionString connectionString = new ConnectionString(connectionUri);
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
        return new MongoTemplate(mongoClient(), database);
    }

    @Bean
    public BeforeConvertCallback<AbstractBaseEntity> beforeSaveCallback() {
        return (entity, collection) -> {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        };
    }
}
