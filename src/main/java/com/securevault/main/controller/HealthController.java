package com.securevault.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.main.configuration.api.ApiVersion;
import com.securevault.main.util.ApiEndpoints;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(ApiEndpoints.HEALTH_BASE_URL)
@ApiVersion("1")
public class HealthController {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@GetMapping(ApiEndpoints.HEALTH_CHECK_URL)
	public ResponseEntity<Map<String, Object>> healthCheck() {
		Map<String, Object> healthStatus = new HashMap<>();
		healthStatus.put("status", "UP");
		healthStatus.put("timestamp", System.currentTimeMillis());

		// Check MongoDB connection
		try {
			mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
			healthStatus.put("mongodb", "UP");
		} catch (Exception e) {
			healthStatus.put("mongodb", "DOWN");
			healthStatus.put("mongodb_error", e.getMessage());
		}

		// Check Redis connection
		try {
			redisConnectionFactory.getConnection().ping();
			healthStatus.put("redis", "UP");
		} catch (Exception e) {
			healthStatus.put("redis", "DOWN");
			healthStatus.put("redis_error", e.getMessage());
		}

		// If any service is down, mark overall status as DOWN
		if (healthStatus.containsValue("DOWN")) {
			healthStatus.put("status", "DOWN");
			return ResponseEntity.status(503).body(healthStatus);
		}

		return ResponseEntity.ok(healthStatus);
	}

	@GetMapping(ApiEndpoints.HEALTH_LIVENESS_URL)
	public ResponseEntity<Map<String, Object>> livenessCheck() {
		Map<String, Object> status = new HashMap<>();
		status.put("status", "UP");
		status.put("timestamp", System.currentTimeMillis());
		return ResponseEntity.ok(status);
	}

	@GetMapping(ApiEndpoints.HEALTH_READINESS_URL)
	public ResponseEntity<Map<String, Object>> readinessCheck() {
		return healthCheck();
	}
}