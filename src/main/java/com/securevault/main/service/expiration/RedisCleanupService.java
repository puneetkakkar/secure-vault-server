package com.securevault.main.service.expiration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis cleanup service that removes stale indexed data when entities with TTL
 * expire.
 * This service uses the RedisEntityDiscoveryService to find entities that need
 * cleanup
 * and performs the actual cleanup operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCleanupService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisEntityDiscoveryService redisEntityDiscoveryService;

    /**
     * Perform cleanup of stale Redis indexed data
     * This method uses the discovery service to find entities and cleans up expired
     * indexed fields
     */
    public CleanupResult performCleanup() {
        CleanupResult result = new CleanupResult();

        try {
            log.debug("Starting Redis cleanup...");

            // Discover all Redis entities using the dedicated discovery service
            redisEntityDiscoveryService.discoverRedisEntities();

            // Get entities that need cleanup
            Map<Class<?>, RedisEntityDiscoveryService.EntityMetadata> entitiesToClean = redisEntityDiscoveryService
                    .getEntitiesNeedingCleanup();

            // Clean up each entity that needs cleanup
            for (Map.Entry<Class<?>, RedisEntityDiscoveryService.EntityMetadata> entry : entitiesToClean.entrySet()) {
                Class<?> entityClass = entry.getKey();
                RedisEntityDiscoveryService.EntityMetadata metadata = entry.getValue();

                try {
                    int cleaned = cleanupEntity(entityClass, metadata);
                    if (cleaned > 0) {
                        result.incrementEntitiesProcessed();
                        result.addCleanedEntries(cleaned);
                        log.debug("Cleaned {} stale index entries for entity: {}",
                                cleaned, entityClass.getSimpleName());
                    }
                } catch (Exception e) {
                    log.error("Error cleaning up entity: {}", entityClass.getSimpleName(), e);
                    result.incrementErrors();
                }
            }

            log.debug("Redis cleanup completed. Processed {} entities, cleaned {} entries",
                    result.getEntitiesProcessed(), result.getIndexEntriesCleaned());

        } catch (Exception e) {
            log.error("Error during Redis cleanup", e);
            result.incrementErrors();
        }

        return result;
    }

    /**
     * Clean up a specific entity type
     * 
     * @param entityClass The entity class
     * @param metadata    Entity metadata
     * @return Number of cleaned index entries
     */
    private int cleanupEntity(Class<?> entityClass, RedisEntityDiscoveryService.EntityMetadata metadata) {
        try {
            int totalCleaned = 0;
            String hashName = metadata.getHashName();

            // Clean up stale entity hashes and update main set
            totalCleaned += cleanupStaleEntityHashes(hashName);
            
            // Also clean up any orphaned entity hashes that aren't in the main set
            // totalCleaned += cleanupOrphanedEntityHashes(hashName);
            
            // Clean up indexed fields
            for (String fieldName : metadata.getIndexedFieldNames()) {
                try {
                    int cleaned = cleanupIndexedField(hashName, fieldName);
                    totalCleaned += cleaned;
                } catch (Exception e) {
                    log.warn("Error cleaning up indexed field: {}", fieldName, e);
                }
            }

            return totalCleaned;

        } catch (Exception e) {
            log.error("Error cleaning up entity: {}", entityClass.getSimpleName(), e);
            return 0;
        }
    }

    /**
     * Clean up stale entity hashes and remove their IDs from the main set
     * 
     * @param hashName The hash name (e.g., "jwt_tokens")
     * @return Number of cleaned entries
     */
    private int cleanupStaleEntityHashes(String hashName) {
        int totalCleaned = 0;

        try {
            // Get connection factory
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory == null) {
                log.warn("Redis connection factory is null, skipping entity hash cleanup");
                return 0;
            }

            // Get all entity IDs from the main set
            Set<byte[]> entityIdBytes = connectionFactory
                    .getConnection()
                    .setCommands()
                    .sMembers(hashName.getBytes());

            if (entityIdBytes == null || entityIdBytes.isEmpty()) {
                return 0;
            }

            // Check each entity ID to see if the corresponding entity hash is expired
            for (byte[] entityIdByteArray : entityIdBytes) {
                String entityId = new String(entityIdByteArray);
                String entityHashKey = hashName + ":" + entityId + ":idx";

                // Check if the entity hash key exists and get its TTL
                Boolean exists = redisTemplate.hasKey(entityHashKey);
                Long ttl = redisTemplate.getExpire(entityHashKey);

                log.debug("Entity hash key: {}, exists: {}, ttl: {}", entityHashKey, exists, ttl);

                // If the key doesn't exist or has expired TTL, clean it up
                if (exists != null && !exists) {
                    // Remove the entity hash if it still exists but is expired
                    // if (exists != null && exists) {
                    redisTemplate.delete(entityHashKey);
                    // }

                    // Remove from main set
                    Long removed = connectionFactory
                            .getConnection()
                            .setCommands()
                            .sRem(hashName.getBytes(), entityIdByteArray);

                    if (removed != null && removed > 0) {
                        totalCleaned += removed;
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Error cleaning up entity hashes: {}", hashName, e);
        }

        return totalCleaned;
    }

    /**
     * Clean up orphaned entity hashes that exist but aren't in the main set
     * 
     * @param hashName The hash name (e.g., "jwt_tokens")
     * @return Number of cleaned entries
     */
    private int cleanupOrphanedEntityHashes(String hashName) {
        int totalCleaned = 0;

        try {
            // Get all entity hash keys that match the pattern
            String entityHashPattern = hashName + ":*:idx";
            Set<String> entityHashKeys = redisTemplate.keys(entityHashPattern);

            if (entityHashKeys == null || entityHashKeys.isEmpty()) {
                return 0;
            }

            // Get all entity IDs from the main set
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory == null) {
                log.warn("Redis connection factory is null, skipping orphaned entity hash cleanup");
                return 0;
            }

            Set<byte[]> mainSetEntityIds = connectionFactory
                    .getConnection()
                    .setCommands()
                    .sMembers(hashName.getBytes());

            Set<String> validEntityIds = new HashSet<>();
            if (mainSetEntityIds != null) {
                for (byte[] entityIdBytes : mainSetEntityIds) {
                    validEntityIds.add(new String(entityIdBytes));
                }
            }

            // Check each entity hash key
            for (String entityHashKey : entityHashKeys) {
                try {
                    // Extract entity ID from the key (e.g., "jwt_tokens:UUID:idx" -> "UUID")
                    String[] parts = entityHashKey.split(":");
                    if (parts.length >= 2) {
                        String entityId = parts[1];

                        // If this entity ID is not in the main set, the hash is orphaned
                        if (!validEntityIds.contains(entityId)) {
                            // Check if the hash is expired or has no TTL
                            Long ttl = redisTemplate.getExpire(entityHashKey);
                            if (ttl == null || ttl <= 0) {
                                Boolean deleted = redisTemplate.delete(entityHashKey);
                                if (deleted != null && deleted) {
                                    totalCleaned++;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error processing orphaned entity hash: {}", entityHashKey, e);
                }
            }

        } catch (Exception e) {
            log.warn("Error cleaning up orphaned entity hashes: {}", hashName, e);
        }

        return totalCleaned;
    }

    /**
     * Clean up indexed fields by removing stale entity IDs from index sets
     * 
     * @param hashName  The hash name (e.g., "jwt_tokens")
     * @param fieldName The field name (e.g., "userId")
     * @return Number of cleaned index entries
     */
    private int cleanupIndexedField(String hashName, String fieldName) {
        int totalCleaned = 0;
        int emptyIndexKeysRemoved = 0;

        try {
            // Get all index keys for this field (Spring Data Redis pattern: hashName:fieldName:value)
            String indexKeyPattern = hashName + ":" + fieldName + ":*";
            Set<String> indexKeys = redisTemplate.keys(indexKeyPattern);

            if (indexKeys == null || indexKeys.isEmpty()) {
                return 0;
            }

            // Get connection factory once for this field
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory == null) {
                log.warn("Redis connection factory is null, skipping field: {}", fieldName);
                return 0;
            }

            for (String indexKey : indexKeys) {
                try {
                    // Get all entity IDs in this index set
                    Set<byte[]> entityIdBytes = connectionFactory
                            .getConnection()
                            .setCommands()
                            .sMembers(indexKey.getBytes());

                    if (entityIdBytes == null || entityIdBytes.isEmpty()) {
                        // Index set is already empty, remove the index key
                        Boolean deleted = redisTemplate.delete(indexKey);
                        if (deleted != null && deleted) {
                            emptyIndexKeysRemoved++;
                        }
                        continue;
                    }

                    // Check each entity ID to see if the corresponding entity hash still exists
                    for (byte[] entityIdByteArray : entityIdBytes) {
                        String entityId = new String(entityIdByteArray);
                        String entityHashKey = hashName + ":" + entityId;

                        // Check if the entity hash key exists
                        Boolean exists = redisTemplate.hasKey(entityHashKey);

                        if (exists != null && !exists) {
                            // Entity hash doesn't exist (expired), remove from index set
                            Long removed = connectionFactory
                                    .getConnection()
                                    .setCommands()
                                    .sRem(indexKey.getBytes(), entityIdByteArray);

                            if (removed != null && removed > 0) {
                                totalCleaned += removed;
                            }
                        }
                    }

                    // After cleanup, check if the index set is now empty and remove it if so
                    Set<byte[]> remainingEntityIds = connectionFactory
                            .getConnection()
                            .setCommands()
                            .sMembers(indexKey.getBytes());

                    if (remainingEntityIds == null || remainingEntityIds.isEmpty()) {
                        // Index set is now empty after cleanup, remove the index key
                        Boolean deleted = redisTemplate.delete(indexKey);
                        if (deleted != null && deleted) {
                            emptyIndexKeysRemoved++;
                        }
                    }

                } catch (Exception e) {
                    log.warn("Error processing index key: {}", indexKey, e);
                }
            }



        } catch (Exception e) {
            log.warn("Error cleaning up indexed field: {}", fieldName, e);
        }

        return totalCleaned;
    }

    /**
     * Result class for cleanup operations
     */
    public static class CleanupResult {
        private int entitiesProcessed = 0;
        private int indexEntriesCleaned = 0;
        private int errors = 0;
        private long startTime = System.currentTimeMillis();

        public int getEntitiesProcessed() {
            return entitiesProcessed;
        }

        public void incrementEntitiesProcessed() {
            this.entitiesProcessed += 1;
        }

        public int getIndexEntriesCleaned() {
            return indexEntriesCleaned;
        }

        public void addCleanedEntries(int count) {
            this.indexEntriesCleaned += count;
        }

        public int getErrors() {
            return errors;
        }

        public void incrementErrors() {
            this.errors += 1;
        }

        public long getDurationMs() {
            return System.currentTimeMillis() - startTime;
        }
    }
}