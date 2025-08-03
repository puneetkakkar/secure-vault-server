package com.securevault.main.service.expiration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for discovering Redis entities and extracting their
 * metadata.
 * This service scans the classpath for classes with @RedisHash annotation
 * and analyzes their TTL and indexed field configurations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisEntityDiscoveryService {

    // Cache of discovered entity metadata to avoid repeated reflection
    private final Map<Class<?>, EntityMetadata> entityMetadataCache = new ConcurrentHashMap<>();

    // Configuration properties for Redis entity discovery
    private final RedisEntityDiscoveryProperties discoveryProperties;

    /**
     * Entity metadata for cleanup operations
     */
    public static class EntityMetadata {
        private final String hashName;
        private final Set<String> indexedFieldNames;
        private final boolean hasTtl;

        public EntityMetadata(String hashName, Set<String> indexedFieldNames, boolean hasTtl) {
            this.hashName = hashName;
            this.indexedFieldNames = indexedFieldNames;
            this.hasTtl = hasTtl;
        }

        public String getHashName() {
            return hashName;
        }

        public Set<String> getIndexedFieldNames() {
            return indexedFieldNames;
        }

        public boolean hasTtl() {
            return hasTtl;
        }

        public boolean needsCleanup() {
            return hasTtl && !indexedFieldNames.isEmpty();
        }
    }

    /**
     * Discover all Redis entities in the classpath
     * This method scans for classes with @RedisHash annotation and extracts their
     * metadata
     */
    public void discoverRedisEntities() {
        try {
            log.debug("Discovering Redis entities...");

            // Clear previous cache
            entityMetadataCache.clear();

            int discoveredCount = 0;

            // Scan each package pattern for Redis entities
            for (String packagePattern : discoveryProperties.getPackagePatterns()) {
                try {
                    Set<Class<?>> redisEntities = scanPackageForRedisEntities(packagePattern);

                    for (Class<?> entityClass : redisEntities) {
                        try {
                            EntityMetadata metadata = extractEntityMetadata(entityClass);
                            entityMetadataCache.put(entityClass, metadata);

                            if (metadata.needsCleanup()) {
                                discoveredCount++;
                                log.info(
                                        "Discovered Redis entity for cleanup: {} (hash: {}, indexed fields: {}, has TTL: {})",
                                        entityClass.getSimpleName(),
                                        metadata.getHashName(),
                                        metadata.getIndexedFieldNames().size(),
                                        metadata.hasTtl());
                            } else {
                                log.debug("Discovered Redis entity (no cleanup needed): {} (hash: {})",
                                        entityClass.getSimpleName(), metadata.getHashName());
                            }
                        } catch (Exception e) {
                            log.warn("Error processing Redis entity: {}", entityClass.getName(), e);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error scanning package: {}", packagePattern, e);
                }
            }

            log.info("Redis entity discovery completed. Found {} entities requiring cleanup.", discoveredCount);

        } catch (Exception e) {
            log.error("Error during Redis entity discovery", e);
        }
    }

    /**
     * Scan a package for classes with @RedisHash annotation
     * 
     * @param packageName The package to scan
     * @return Set of classes with @RedisHash annotation
     */
    private Set<Class<?>> scanPackageForRedisEntities(String packageName) {
        Set<Class<?>> redisEntities = new HashSet<>();

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);

            String packageSearchPath = "classpath*:" + packageName.replace('.', '/') + "/**/*.class";
            Resource[] resources = resolver.getResources(packageSearchPath);

            for (Resource resource : resources) {
                try {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();

                    // Check if the class has @RedisHash annotation
                    if (metadataReader.getAnnotationMetadata().hasAnnotation(RedisHash.class.getName())) {
                        Class<?> clazz = ClassUtils.forName(className, getClass().getClassLoader());
                        redisEntities.add(clazz);
                        log.debug("Found Redis entity class: {}", className);
                    }
                } catch (Exception e) {
                    log.debug("Error processing resource: {}", resource.getFilename(), e);
                }
            }
        } catch (Exception e) {
            log.warn("Error scanning package {} for Redis entities", packageName, e);
        }

        return redisEntities;
    }

    /**
     * Extract metadata from a Redis entity class
     * 
     * @param entityClass The entity class to analyze
     * @return EntityMetadata containing information about the entity
     */
    public EntityMetadata extractEntityMetadata(Class<?> entityClass) {
        Set<String> indexedFields = new HashSet<>();
        boolean hasTtl = false;
        String hashName = null;

        // Get hash name from @RedisHash annotation
        if (entityClass.isAnnotationPresent(RedisHash.class)) {
            RedisHash redisHash = entityClass.getAnnotation(RedisHash.class);
            hashName = redisHash.value();
        }

        log.debug("Processing entity: {}, Hash name: {}", entityClass.getSimpleName(), hashName);

        // Check all fields including inherited ones
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Indexed.class)) {
                    indexedFields.add(field.getName());
                }

                if (field.isAnnotationPresent(TimeToLive.class)) {
                    hasTtl = true;
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        log.debug("Entity: {}, Indexed fields: {}, Has TTL: {}",
                entityClass.getSimpleName(), indexedFields, hasTtl);

        return new EntityMetadata(hashName, indexedFields, hasTtl);
    }

    /**
     * Get all discovered entity metadata
     * 
     * @return Map of entity classes to their metadata
     */
    public Map<Class<?>, EntityMetadata> getAllEntityMetadata() {
        return new HashMap<>(entityMetadataCache);
    }

    /**
     * Get discovered entity metadata as a map for REST responses
     * 
     * @return Map of entity class names to their metadata
     */
    public Map<String, Map<String, Object>> getDiscoveredEntities() {
        Map<String, Map<String, Object>> result = new HashMap<>();

        for (Map.Entry<Class<?>, EntityMetadata> entry : entityMetadataCache.entrySet()) {
            Class<?> entityClass = entry.getKey();
            EntityMetadata metadata = entry.getValue();

            Map<String, Object> entityInfo = new HashMap<>();
            entityInfo.put("hashName", metadata.getHashName());
            entityInfo.put("indexedFields", metadata.getIndexedFieldNames());
            entityInfo.put("hasTtl", metadata.hasTtl());
            entityInfo.put("needsCleanup", metadata.needsCleanup());

            result.put(entityClass.getSimpleName(), entityInfo);
        }

        return result;
    }

    /**
     * Get entities that need cleanup (have both TTL and indexed fields)
     * 
     * @return Map of entity classes to their metadata
     */
    public Map<Class<?>, EntityMetadata> getEntitiesNeedingCleanup() {
        Map<Class<?>, EntityMetadata> result = new HashMap<>();

        for (Map.Entry<Class<?>, EntityMetadata> entry : entityMetadataCache.entrySet()) {
            if (entry.getValue().needsCleanup()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    /**
     * Check if a class has TTL and indexed fields
     * 
     * @param clazz The class to check
     * @return true if the class has both TTL and indexed fields
     */
    public static boolean hasTtlAndIndexedFields(Class<?> clazz) {
        boolean hasTtl = false;
        boolean hasIndexed = false;

        // Check all fields including inherited ones
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(TimeToLive.class)) {
                    hasTtl = true;
                }
                if (field.isAnnotationPresent(Indexed.class)) {
                    hasIndexed = true;
                }

                // If we found both, we can stop searching
                if (hasTtl && hasIndexed) {
                    return true;
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return hasTtl && hasIndexed;
    }

    /**
     * Clear the entity metadata cache (useful for testing or re-discovery)
     */
    public void clearCache() {
        entityMetadataCache.clear();
        log.debug("Entity metadata cache cleared");
    }
}