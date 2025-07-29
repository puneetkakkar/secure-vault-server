package com.securevault.main.service.expiration;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled task for cleaning up Redis indexed fields.
 * Runs every 10 minutes by default and can be configured via properties.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCleanupScheduler {

    private final RedisCleanupService redisCleanupService;
    private final RedisEntityDiscoveryService redisEntityDiscoveryService;

    @Value("${app.redis.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${app.redis.cleanup.interval:600000}") // 10 minutes in milliseconds
    private long cleanupInterval;

    /**
     * Initialize and log scheduler configuration
     */
    @PostConstruct
    public void initialize() {
        log.info("Redis Cleanup Scheduler initialized - Enabled: {}, Interval: {}ms ({} seconds)",
                cleanupEnabled, cleanupInterval, cleanupInterval / 1000);

        if (cleanupEnabled) {
            log.info("Scheduled Redis cleanup will run every {} seconds", cleanupInterval / 1000);
        } else {
            log.warn("Redis cleanup is DISABLED. No automatic cleanup will occur.");
        }
    }

    /**
     * Scheduled task to perform Redis index cleanup.
     * Runs every 10 minutes by default, configurable via app.redis.cleanup.interval
     */
    @Scheduled(fixedRateString = "${app.redis.cleanup.interval:600000}")
    public void scheduledCleanup() {
        log.debug("Scheduled cleanup triggered. Enabled: {}, Interval: {}ms", cleanupEnabled, cleanupInterval);

        if (!cleanupEnabled) {
            log.debug("Redis cleanup is disabled. Skipping scheduled cleanup.");
            return;
        }

        try {
            log.info("Starting scheduled Redis cleanup...");

            long startTime = System.currentTimeMillis();

            // Perform the cleanup
            RedisCleanupService.CleanupResult result = redisCleanupService.performCleanup();

            long duration = System.currentTimeMillis() - startTime;

            // Log the results
            if (result.getErrors() > 0) {
                log.warn(
                        "Scheduled cleanup completed with {} errors. Processed {} entities, cleaned {} entries in {}ms",
                        result.getErrors(), result.getEntitiesProcessed(), result.getIndexEntriesCleaned(), duration);
            } else if (result.getIndexEntriesCleaned() > 0) {
                log.info("Scheduled cleanup completed successfully. Processed {} entities, cleaned {} entries in {}ms",
                        result.getEntitiesProcessed(), result.getIndexEntriesCleaned(), duration);
            } else {
                log.debug("Scheduled cleanup completed. No stale data found to clean up. Duration: {}ms", duration);
            }

        } catch (Exception e) {
            log.error("Error during scheduled Redis cleanup", e);
        }
    }

    /**
     * Clear the entity discovery cache (useful for testing or re-discovery)
     */
    public void clearDiscoveryCache() {
        redisEntityDiscoveryService.clearCache();
    }

}