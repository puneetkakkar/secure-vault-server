package com.securevault.main.service.expiration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import com.securevault.main.entity.JwtToken;

@ExtendWith(MockitoExtension.class)
class RedisEntityDiscoveryServiceTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private RedisEntityDiscoveryProperties discoveryProperties;

    private RedisEntityDiscoveryService discoveryService;

    @BeforeEach
    void setUp() {
        lenient().when(discoveryProperties.getPackagePatterns()).thenReturn(List.of("com.securevault.main.entity"));
        discoveryService = new RedisEntityDiscoveryService(discoveryProperties);
    }

    @Test
    void testDiscoverRedisEntities() {
        // When
        discoveryService.discoverRedisEntities();

        // Then
        Map<String, Map<String, Object>> entities = discoveryService.getDiscoveredEntities();
        assertNotNull(entities);
        // Should find at least the JwtToken entity
        assertTrue(entities.size() >= 1);
        assertTrue(entities.containsKey("JwtToken"));
    }

    @Test
    void testExtractEntityMetadata() {
        // When
        RedisEntityDiscoveryService.EntityMetadata metadata = discoveryService.extractEntityMetadata(JwtToken.class);

        // Then
        assertNotNull(metadata);
        assertEquals("jwt_tokens", metadata.getHashName());
        assertTrue(metadata.hasTtl());
        assertTrue(metadata.needsCleanup());
        assertTrue(metadata.getIndexedFieldNames().size() > 0);
    }

    @Test
    void testHasTtlAndIndexedFields() {
        // Test JWT token class
        assertTrue(RedisEntityDiscoveryService.hasTtlAndIndexedFields(JwtToken.class));

        // Test a class without annotations
        assertFalse(RedisEntityDiscoveryService.hasTtlAndIndexedFields(String.class));
    }

    @Test
    void testGetEntitiesNeedingCleanup() {
        // Given
        discoveryService.discoverRedisEntities();

        // When
        Map<Class<?>, RedisEntityDiscoveryService.EntityMetadata> entitiesNeedingCleanup = discoveryService
                .getEntitiesNeedingCleanup();

        // Then
        assertNotNull(entitiesNeedingCleanup);
        // Should find at least the JwtToken entity that needs cleanup
        assertTrue(entitiesNeedingCleanup.size() >= 1);
        assertTrue(entitiesNeedingCleanup.containsKey(JwtToken.class));
    }

    @Test
    void testEntityMetadata() {
        // Given
        RedisEntityDiscoveryService.EntityMetadata metadata = new RedisEntityDiscoveryService.EntityMetadata(
                "test_hash",
                java.util.Set.of("field1", "field2"),
                true);

        // Then
        assertEquals("test_hash", metadata.getHashName());
        assertEquals(2, metadata.getIndexedFieldNames().size());
        assertTrue(metadata.hasTtl());
        assertTrue(metadata.needsCleanup());
    }

    @Test
    void testEntityMetadataNoCleanup() {
        // Given
        RedisEntityDiscoveryService.EntityMetadata metadata = new RedisEntityDiscoveryService.EntityMetadata(
                "test_hash",
                java.util.Set.of(),
                false);

        // Then
        assertEquals("test_hash", metadata.getHashName());
        assertTrue(metadata.getIndexedFieldNames().isEmpty());
        assertFalse(metadata.hasTtl());
        assertFalse(metadata.needsCleanup());
    }
}