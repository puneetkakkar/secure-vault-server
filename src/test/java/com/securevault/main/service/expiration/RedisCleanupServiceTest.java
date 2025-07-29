package com.securevault.main.service.expiration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import com.securevault.main.entity.JwtToken;

@ExtendWith(MockitoExtension.class)
class RedisCleanupServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private RedisEntityDiscoveryService entityDiscoveryService;

    private RedisCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        cleanupService = new RedisCleanupService(redisTemplate, entityDiscoveryService);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void testPerformCleanup() {
        // Given
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"jwtToken"});
        when(applicationContext.getType("jwtToken")).thenReturn(null);
        when(redisTemplate.keys("jwt_tokens:*")).thenReturn(Set.of("jwt_tokens:test-id"));
        when(redisTemplate.getExpire("jwt_tokens:test-id")).thenReturn(0L); // Expired
        when(redisTemplate.keys("jwt_tokens:userId:*")).thenReturn(Set.of("jwt_tokens:userId:123"));
        when(setOperations.remove(anyString(), anyString())).thenReturn(1L);

        // When
        RedisCleanupService.CleanupResult result = cleanupService.performCleanup();

        // Then
        assertNotNull(result);
        assertEquals(0, result.getEntitiesProcessed());
        assertEquals(0, result.getIndexEntriesCleaned());
        assertEquals(0, result.getErrors());
    }

    @Test
    void testPerformCleanupNoExpiredTokens() {
        // Given
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"jwtToken"});
        when(applicationContext.getType("jwtToken")).thenReturn(null);
        when(redisTemplate.keys("jwt_tokens:*")).thenReturn(Set.of("jwt_tokens:test-id"));
        when(redisTemplate.getExpire("jwt_tokens:test-id")).thenReturn(3600L); // Not expired

        // When
        RedisCleanupService.CleanupResult result = cleanupService.performCleanup();

        // Then
        assertNotNull(result);
        assertEquals(0, result.getEntitiesProcessed());
        assertEquals(0, result.getIndexEntriesCleaned());
        assertEquals(0, result.getErrors());
    }

    @Test
    void testPerformCleanupNoTokens() {
        // Given
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"jwtToken"});
        when(applicationContext.getType("jwtToken")).thenReturn(null);
        when(redisTemplate.keys("jwt_tokens:*")).thenReturn(Set.of());

        // When
        RedisCleanupService.CleanupResult result = cleanupService.performCleanup();

        // Then
        assertNotNull(result);
        assertEquals(0, result.getEntitiesProcessed());
        assertEquals(0, result.getIndexEntriesCleaned());
        assertEquals(0, result.getErrors());
    }

    @Test
    void testHasTtlAndIndexedFields() {
        // Test JWT token class
        assertTrue(RedisEntityDiscoveryService.hasTtlAndIndexedFields(JwtToken.class));
        
        // Test a class without annotations
        assertFalse(RedisEntityDiscoveryService.hasTtlAndIndexedFields(String.class));
    }

    @Test
    void testGetDiscoveredEntities() {
        // Given
        when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"jwtToken"});
        when(applicationContext.getType("jwtToken")).thenReturn(null);

        // When
        java.util.Map<String, java.util.Map<String, Object>> entities = entityDiscoveryService.getDiscoveredEntities();

        // Then
        assertNotNull(entities);
        assertTrue(entities.isEmpty());
    }

    @Test
    void testCleanupResult() {
        // Given
        RedisCleanupService.CleanupResult result = new RedisCleanupService.CleanupResult();

        // When
        result.incrementEntitiesProcessed();
        result.addCleanedEntries(5);
        result.incrementErrors();

        // Then
        assertEquals(1, result.getEntitiesProcessed());
        assertEquals(5, result.getIndexEntriesCleaned());
        assertEquals(1, result.getErrors());
        assertTrue(result.getDurationMs() >= 0);
    }
} 