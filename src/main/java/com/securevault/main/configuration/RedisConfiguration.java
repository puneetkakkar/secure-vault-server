package com.securevault.main.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {

	@Value("${spring.data.redis.mode:standalone}")
	private String redisMode;

	@Value("${spring.data.redis.database:0}")
	private String database;

	@Value("${spring.data.redis.host:localhost}")
	private String host;

	@Value("${spring.data.redis.port:6379}")
	private String port;

	@Value("${spring.data.redis.password}")
	private String password;

	@Value("${spring.data.redis.timeout:2000}")
	private String timeout;

	private final RedisProperties redisProperties;

	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory() {
		if ("sentinel".equalsIgnoreCase(redisMode)) {
			return createSentinelConnectionFactory();
		} else {
			return createStandaloneConnectionFactory();
		}
	}

	private LettuceConnectionFactory createSentinelConnectionFactory() {
		RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
		sentinelConfig.setMaster(redisProperties.getSentinel().getMaster());

		redisProperties.getSentinel().getNodes().forEach(node -> {
			String[] parts = node.split(":");
			if (parts.length == 2) {
				sentinelConfig.sentinel(parts[0], Integer.parseInt(parts[1]));
			}
		});

		if (password != null && !password.isBlank()) {
			sentinelConfig.setPassword(RedisPassword.of(password));
		}

		LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
				.readFrom(ReadFrom.REPLICA_PREFERRED)
				.commandTimeout(Duration.ofMillis(Long.parseLong(timeout)))
				.build();

		return new LettuceConnectionFactory(sentinelConfig, lettuceClientConfiguration);
	}

	private LettuceConnectionFactory createStandaloneConnectionFactory() {
		RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
		standaloneConfig.setDatabase(Integer.parseInt(database));
		standaloneConfig.setHostName(host);
		standaloneConfig.setPort(Integer.parseInt(port));

		if (password != null && !password.isBlank()) {
			standaloneConfig.setPassword(RedisPassword.of(password));
		}

		LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
				.commandTimeout(Duration.ofMillis(Long.parseLong(timeout)))
				.build();

		return new LettuceConnectionFactory(standaloneConfig, lettuceClientConfiguration);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		final RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		template.afterPropertiesSet();
		return template;
	}
}
