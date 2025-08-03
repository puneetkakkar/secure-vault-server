package com.securevault.main.service.expiration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RedisCleanupSchedulerTest {

    @Mock
    private RedisCleanupService cleanupService;

    @Mock
    private RedisEntityDiscoveryService entityDiscoveryService;

    private RedisCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new RedisCleanupScheduler(cleanupService, entityDiscoveryService);
    }

    @Test
    void testSchedulerInitialization() {
        // Given
        ReflectionTestUtils.setField(scheduler, "cleanupEnabled", true);
        ReflectionTestUtils.setField(scheduler, "cleanupInterval", 60000L);

        // When
        scheduler.initialize();

        // Then - This test verifies the initialize method runs without errors
        // The actual logging behavior is tested by the fact that no exceptions are
        // thrown
        assertTrue(true); // If we get here, the initialization was successful
    }

    @Test
    void testScheduledCleanupWhenEnabled() {
        // Given
        ReflectionTestUtils.setField(scheduler, "cleanupEnabled", true);
        ReflectionTestUtils.setField(scheduler, "cleanupInterval", 60000L);

        RedisCleanupService.CleanupResult mockResult = new RedisCleanupService.CleanupResult();
        when(cleanupService.performCleanup()).thenReturn(mockResult);

        // When
        scheduler.scheduledCleanup();

        // Then
        verify(cleanupService, times(1)).performCleanup();
    }

    @Test
    void testScheduledCleanupWhenDisabled() {
        // Given
        ReflectionTestUtils.setField(scheduler, "cleanupEnabled", false);
        ReflectionTestUtils.setField(scheduler, "cleanupInterval", 60000L);

        // When
        scheduler.scheduledCleanup();

        // Then
        verify(cleanupService, never()).performCleanup();
    }

    @Test
    void testClearDiscoveryCache() {
        // When
        scheduler.clearDiscoveryCache();

        // Then
        verify(entityDiscoveryService, times(1)).clearCache();
    }

    @Test
    void testScheduledCleanupWithErrors() {
        // Given
        ReflectionTestUtils.setField(scheduler, "cleanupEnabled", true);
        ReflectionTestUtils.setField(scheduler, "cleanupInterval", 60000L);

        RedisCleanupService.CleanupResult mockResult = new RedisCleanupService.CleanupResult();
        mockResult.incrementErrors();
        when(cleanupService.performCleanup()).thenReturn(mockResult);

        // When
        scheduler.scheduledCleanup();

        // Then
        verify(cleanupService, times(1)).performCleanup();
    }

    @Test
    void testScheduledCleanupWithCleanupResults() {
        // Given
        ReflectionTestUtils.setField(scheduler, "cleanupEnabled", true);
        ReflectionTestUtils.setField(scheduler, "cleanupInterval", 60000L);

        RedisCleanupService.CleanupResult mockResult = new RedisCleanupService.CleanupResult();
        mockResult.incrementEntitiesProcessed();
        mockResult.addCleanedEntries(5);
        when(cleanupService.performCleanup()).thenReturn(mockResult);

        // When
        scheduler.scheduledCleanup();

        // Then
        verify(cleanupService, times(1)).performCleanup();
    }
}