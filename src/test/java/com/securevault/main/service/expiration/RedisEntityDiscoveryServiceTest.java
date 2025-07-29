package com.securevault.main.service.expiration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private RedisEntityDiscoveryService discoveryService;

    @BeforeEach
    void setUp() {
        discoveryService = new RedisEntityDiscoveryService();
    }

    @Test
    void testDiscoverRedisEntities() {
        // Given
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[] { "jwtToken" });
        when(applicationContext.getType("jwtToken")).thenReturn(null);

        // When
        discoveryService.discoverRedisEntities();

        // Then
        Map<String, Map<String, Object>> entities = discoveryService.getDiscoveredEntities();
        assertNotNull(entities);
        assertTrue(entities.isEmpty());
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
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[] { "jwtToken" });
        when(applicationContext.getType("jwtToken")).thenReturn(null);
        discoveryService.discoverRedisEntities();

        // When
        Map<Class<?>, RedisEntityDiscoveryService.EntityMetadata> entitiesNeedingCleanup = discoveryService
                .getEntitiesNeedingCleanup();

        // Then
        assertNotNull(entitiesNeedingCleanup);
        assertEquals(0, entitiesNeedingCleanup.size());
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