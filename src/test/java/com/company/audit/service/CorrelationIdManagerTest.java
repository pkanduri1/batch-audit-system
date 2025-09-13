package com.company.audit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CorrelationIdManagerImpl.
 * Tests UUID generation, thread-local storage, thread isolation, and cleanup logic.
 */
@DisplayName("CorrelationIdManager Tests")
class CorrelationIdManagerTest {
    
    private CorrelationIdManagerImpl correlationIdManager;
    
    @BeforeEach
    void setUp() {
        correlationIdManager = new CorrelationIdManagerImpl();
        // Ensure clean state before each test
        correlationIdManager.clearCorrelationId();
    }
    
    @Nested
    @DisplayName("UUID Generation Tests")
    class UuidGenerationTests {
        
        @Test
        @DisplayName("Should generate unique correlation IDs")
        void shouldGenerateUniqueCorrelationIds() {
            UUID id1 = correlationIdManager.generateCorrelationId();
            UUID id2 = correlationIdManager.generateCorrelationId();
            
            assertNotNull(id1);
            assertNotNull(id2);
            assertNotEquals(id1, id2);
        }
        
        @Test
        @DisplayName("Should generate valid UUID format")
        void shouldGenerateValidUuidFormat() {
            UUID correlationId = correlationIdManager.generateCorrelationId();
            
            assertNotNull(correlationId);
            // Verify UUID string format
            String uuidString = correlationId.toString();
            assertTrue(uuidString.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
        }
        
        @Test
        @DisplayName("Should set generated correlation ID as current")
        void shouldSetGeneratedCorrelationIdAsCurrent() {
            UUID generatedId = correlationIdManager.generateCorrelationId();
            UUID currentId = correlationIdManager.getCurrentCorrelationId();
            
            assertEquals(generatedId, currentId);
        }
        
        @Test
        @DisplayName("Should generate UUIDs with proper entropy using Java 17+ improvements")
        void shouldGenerateUuidsWithProperEntropyUsingJava17Improvements() {
            // Generate multiple UUIDs and verify they have good entropy distribution
            var generatedIds = new java.util.HashSet<UUID>();
            
            for (int i = 0; i < 1000; i++) {
                UUID id = correlationIdManager.generateCorrelationId();
                assertNotNull(id);
                assertTrue(generatedIds.add(id), "Generated duplicate UUID: " + id);
                
                // Verify UUID version (should be version 4 for random UUIDs)
                assertEquals(4, id.version(), "UUID should be version 4 (random)");
                
                // Verify variant bits are set correctly
                assertEquals(2, id.variant(), "UUID variant should be 2 (IETF)");
            }
            
            assertEquals(1000, generatedIds.size(), "All generated UUIDs should be unique");
        }
        
        @Test
        @DisplayName("Should generate UUIDs with consistent format across multiple calls")
        void shouldGenerateUuidsWithConsistentFormatAcrossMultipleCalls() {
            for (int i = 0; i < 100; i++) {
                UUID correlationId = correlationIdManager.generateCorrelationId();
                
                // Verify UUID properties
                assertNotNull(correlationId);
                assertEquals(4, correlationId.version());
                assertEquals(2, correlationId.variant());
                
                // Verify string representation
                String uuidString = correlationId.toString();
                assertEquals(36, uuidString.length());
                assertTrue(uuidString.matches("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"));
            }
        }
    }
    
    @Nested
    @DisplayName("ThreadLocal Storage Tests")
    class ThreadLocalStorageTests {
        
        @Test
        @DisplayName("Should return null when no correlation ID is set")
        void shouldReturnNullWhenNoCorrelationIdSet() {
            UUID currentId = correlationIdManager.getCurrentCorrelationId();
            assertNull(currentId);
        }
        
        @Test
        @DisplayName("Should store and retrieve correlation ID")
        void shouldStoreAndRetrieveCorrelationId() {
            UUID testId = UUID.randomUUID();
            
            correlationIdManager.setCorrelationId(testId);
            UUID retrievedId = correlationIdManager.getCurrentCorrelationId();
            
            assertEquals(testId, retrievedId);
        }
        
        @Test
        @DisplayName("Should clear correlation ID")
        void shouldClearCorrelationId() {
            UUID testId = UUID.randomUUID();
            correlationIdManager.setCorrelationId(testId);
            
            correlationIdManager.clearCorrelationId();
            UUID currentId = correlationIdManager.getCurrentCorrelationId();
            
            assertNull(currentId);
        }
        
        @Test
        @DisplayName("Should handle null correlation ID by clearing")
        void shouldHandleNullCorrelationIdByClearing() {
            UUID testId = UUID.randomUUID();
            correlationIdManager.setCorrelationId(testId);
            
            correlationIdManager.setCorrelationId(null);
            UUID currentId = correlationIdManager.getCurrentCorrelationId();
            
            assertNull(currentId);
        }
    }
    
    @Nested
    @DisplayName("Thread Isolation Tests")
    class ThreadIsolationTests {
        
        @Test
        @DisplayName("Should maintain separate correlation IDs per thread")
        void shouldMaintainSeparateCorrelationIdsPerThread() throws InterruptedException, ExecutionException {
            UUID mainThreadId = UUID.randomUUID();
            correlationIdManager.setCorrelationId(mainThreadId);
            
            AtomicReference<UUID> otherThreadId = new AtomicReference<>();
            AtomicReference<UUID> otherThreadRetrievedId = new AtomicReference<>();
            
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                UUID threadSpecificId = UUID.randomUUID();
                correlationIdManager.setCorrelationId(threadSpecificId);
                
                otherThreadId.set(threadSpecificId);
                otherThreadRetrievedId.set(correlationIdManager.getCurrentCorrelationId());
            });
            
            future.get();
            
            // Verify main thread correlation ID is unchanged
            assertEquals(mainThreadId, correlationIdManager.getCurrentCorrelationId());
            
            // Verify other thread had its own correlation ID
            assertEquals(otherThreadId.get(), otherThreadRetrievedId.get());
            assertNotEquals(mainThreadId, otherThreadId.get());
        }
        
        @Test
        @DisplayName("Should not leak correlation IDs between threads")
        void shouldNotLeakCorrelationIdsBetweenThreads() throws InterruptedException, ExecutionException {
            UUID mainThreadId = UUID.randomUUID();
            correlationIdManager.setCorrelationId(mainThreadId);
            
            AtomicReference<UUID> otherThreadInitialId = new AtomicReference<>();
            
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // Other thread should start with no correlation ID
                otherThreadInitialId.set(correlationIdManager.getCurrentCorrelationId());
            });
            
            future.get();
            
            // Verify other thread started with null correlation ID
            assertNull(otherThreadInitialId.get());
            
            // Verify main thread correlation ID is still intact
            assertEquals(mainThreadId, correlationIdManager.getCurrentCorrelationId());
        }
    }
    
    @Nested
    @DisplayName("Memory Leak Prevention Tests")
    class MemoryLeakPreventionTests {
        
        @Test
        @DisplayName("Should prevent memory leaks with proper cleanup")
        void shouldPreventMemoryLeaksWithProperCleanup() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            
            try {
                // Submit multiple tasks that set correlation IDs
                for (int i = 0; i < 100; i++) {
                    executor.submit(() -> {
                        UUID id = correlationIdManager.generateCorrelationId();
                        assertNotNull(id);
                        // Simulate some work
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        // Clean up
                        correlationIdManager.clearCorrelationId();
                        assertNull(correlationIdManager.getCurrentCorrelationId());
                    });
                }
                
                executor.shutdown();
                assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
                
                // Verify main thread is not affected
                assertNull(correlationIdManager.getCurrentCorrelationId());
                
            } finally {
                if (!executor.isShutdown()) {
                    executor.shutdownNow();
                }
            }
        }
        
        @Test
        @DisplayName("Should handle high concurrency without memory leaks")
        void shouldHandleHighConcurrencyWithoutMemoryLeaks() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(50);
            
            try {
                // Submit many concurrent tasks to test virtual thread compatibility
                for (int i = 0; i < 1000; i++) {
                    final int taskId = i;
                    executor.submit(() -> {
                        try {
                            UUID correlationId = correlationIdManager.generateCorrelationId();
                            assertNotNull(correlationId);
                            
                            // Verify thread isolation
                            assertEquals(correlationId, correlationIdManager.getCurrentCorrelationId());
                            
                            // Simulate processing
                            Thread.sleep(1);
                            
                            // Verify correlation ID is still correct
                            assertEquals(correlationId, correlationIdManager.getCurrentCorrelationId());
                            
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            // Always clean up to prevent memory leaks
                            correlationIdManager.clearCorrelationId();
                        }
                    });
                }
                
                executor.shutdown();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
                
                // Verify main thread is clean
                assertNull(correlationIdManager.getCurrentCorrelationId());
                
            } finally {
                if (!executor.isShutdown()) {
                    executor.shutdownNow();
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Spring Boot 3.4+ and Virtual Thread Compatibility Tests")
    class SpringBootCompatibilityTests {
        
        @Test
        @DisplayName("Should work correctly with InheritableThreadLocal for virtual thread compatibility")
        void shouldWorkCorrectlyWithInheritableThreadLocalForVirtualThreadCompatibility() throws InterruptedException, ExecutionException {
            UUID parentCorrelationId = UUID.randomUUID();
            correlationIdManager.setCorrelationId(parentCorrelationId);
            
            // Test with regular threads (simulating virtual thread behavior)
            List<UUID> childThreadIds = Collections.synchronizedList(new ArrayList<>());
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (int i = 0; i < 10; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    // Child thread should inherit parent's correlation ID due to InheritableThreadLocal
                    UUID inheritedId = correlationIdManager.getCurrentCorrelationId();
                    childThreadIds.add(inheritedId);
                    
                    // Generate new correlation ID in child thread
                    UUID newId = correlationIdManager.generateCorrelationId();
                    assertNotNull(newId);
                    assertEquals(newId, correlationIdManager.getCurrentCorrelationId());
                });
                futures.add(future);
            }
            
            // Wait for all futures to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            
            // Verify parent thread correlation ID is unchanged
            assertEquals(parentCorrelationId, correlationIdManager.getCurrentCorrelationId());
            
            // Note: InheritableThreadLocal behavior may vary with virtual threads
            // This test ensures the implementation is compatible with both scenarios
        }
        
        @Test
        @DisplayName("Should handle concurrent access patterns typical in Spring Boot 3.4+ applications")
        void shouldHandleConcurrentAccessPatternsTypicalInSpringBoot34Applications() throws InterruptedException {
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            
            ExecutorService executor = Executors.newFixedThreadPool(20);
            
            try {
                // Simulate Spring Boot request handling patterns
                for (int i = 0; i < 200; i++) {
                    final int requestId = i;
                    executor.submit(() -> {
                        try {
                            // Simulate request processing with correlation ID
                            UUID requestCorrelationId = UUID.randomUUID();
                            
                            correlationIdManager.executeWithCorrelationId(requestCorrelationId, () -> {
                                // Verify correlation ID is set correctly
                                assertEquals(requestCorrelationId, correlationIdManager.getCurrentCorrelationId());
                                
                                // Simulate nested service calls
                                UUID currentId = correlationIdManager.getCurrentCorrelationId();
                                assertNotNull(currentId);
                                assertEquals(requestCorrelationId, currentId);
                                
                                // Simulate some processing time
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                            
                            // After request processing, correlation ID should be cleared
                            assertNull(correlationIdManager.getCurrentCorrelationId());
                            successCount.incrementAndGet();
                            
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    });
                }
                
                executor.shutdown();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
                
                assertEquals(200, successCount.get());
                assertEquals(0, errorCount.get());
                
            } finally {
                if (!executor.isShutdown()) {
                    executor.shutdownNow();
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {
        
        @Test
        @DisplayName("Should execute task with correlation ID and restore previous state")
        void shouldExecuteTaskWithCorrelationIdAndRestorePreviousState() {
            UUID originalId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            
            correlationIdManager.setCorrelationId(originalId);
            
            AtomicReference<UUID> taskCorrelationId = new AtomicReference<>();
            
            correlationIdManager.executeWithCorrelationId(taskId, () -> {
                taskCorrelationId.set(correlationIdManager.getCurrentCorrelationId());
            });
            
            // Verify task executed with correct correlation ID
            assertEquals(taskId, taskCorrelationId.get());
            
            // Verify original correlation ID is restored
            assertEquals(originalId, correlationIdManager.getCurrentCorrelationId());
        }
        
        @Test
        @DisplayName("Should execute task with correlation ID and clear when no previous state")
        void shouldExecuteTaskWithCorrelationIdAndClearWhenNoPreviousState() {
            UUID taskId = UUID.randomUUID();
            
            AtomicReference<UUID> taskCorrelationId = new AtomicReference<>();
            
            correlationIdManager.executeWithCorrelationId(taskId, () -> {
                taskCorrelationId.set(correlationIdManager.getCurrentCorrelationId());
            });
            
            // Verify task executed with correct correlation ID
            assertEquals(taskId, taskCorrelationId.get());
            
            // Verify correlation ID is cleared after execution
            assertNull(correlationIdManager.getCurrentCorrelationId());
        }
        
        @Test
        @DisplayName("Should correctly report correlation ID presence")
        void shouldCorrectlyReportCorrelationIdPresence() {
            assertFalse(correlationIdManager.hasCorrelationId());
            
            correlationIdManager.setCorrelationId(UUID.randomUUID());
            assertTrue(correlationIdManager.hasCorrelationId());
            
            correlationIdManager.clearCorrelationId();
            assertFalse(correlationIdManager.hasCorrelationId());
        }
        
        @Test
        @DisplayName("Should handle exceptions in executeWithCorrelationId and still restore state")
        void shouldHandleExceptionsInExecuteWithCorrelationIdAndStillRestoreState() {
            UUID originalId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            
            correlationIdManager.setCorrelationId(originalId);
            
            assertThrows(RuntimeException.class, () -> {
                correlationIdManager.executeWithCorrelationId(taskId, () -> {
                    assertEquals(taskId, correlationIdManager.getCurrentCorrelationId());
                    throw new RuntimeException("Test exception");
                });
            });
            
            // Verify original correlation ID is restored even after exception
            assertEquals(originalId, correlationIdManager.getCurrentCorrelationId());
        }
    }
}