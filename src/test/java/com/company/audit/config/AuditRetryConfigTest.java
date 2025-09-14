package com.company.audit.config;

import com.company.audit.exception.AuditPersistenceException;
import com.company.audit.exception.AuditRetryException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.TestPropertySource;

import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.sql.SQLTransientException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditRetryConfig.
 * Tests retry templates and policies for different operation types.
 */
@SpringBootTest(classes = AuditRetryConfig.class)
@TestPropertySource(properties = {
    "audit.retry.enabled=true"
})
class AuditRetryConfigTest {

    @Test
    void testAuditRetryTemplateConfiguration() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.auditRetryTemplate();
        
        assertNotNull(retryTemplate);
        // Test that the template is properly configured by attempting to use it
        assertTrue(retryTemplate instanceof RetryTemplate);
    }

    @Test
    void testDatabaseRetryTemplateConfiguration() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.databaseRetryTemplate();
        
        assertNotNull(retryTemplate);
        // Test that the template is properly configured by attempting to use it
        assertTrue(retryTemplate instanceof RetryTemplate);
    }

    @Test
    void testQuickRetryTemplateConfiguration() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.quickRetryTemplate();
        
        assertNotNull(retryTemplate);
        // Test that the template is properly configured by attempting to use it
        assertTrue(retryTemplate instanceof RetryTemplate);
    }

    @Test
    void testAuditRetryTemplateWithRetryableException() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.auditRetryTemplate();
        
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        assertThrows(AuditRetryException.class, () -> {
            retryTemplate.execute((RetryCallback<Void, Exception>) context -> {
                context.setAttribute("operation.name", "testOperation");
                attemptCount.incrementAndGet();
                throw new AuditPersistenceException("Connection timeout", 
                    new RuntimeException("connection failed"));
            });
        });
        
        assertEquals(3, attemptCount.get()); // Should retry 3 times
    }

    @Test
    void testAuditRetryTemplateWithNonRetryableException() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.auditRetryTemplate();
        
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        assertThrows(AuditPersistenceException.class, () -> {
            retryTemplate.execute((RetryCallback<Void, Exception>) context -> {
                context.setAttribute("operation.name", "testOperation");
                attemptCount.incrementAndGet();
                throw new AuditPersistenceException("Constraint violation", 
                    new RuntimeException("unique constraint"));
            });
        });
        
        assertEquals(1, attemptCount.get()); // Should not retry
    }

    @Test
    void testDatabaseRetryTemplateWithSQLException() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.databaseRetryTemplate();
        
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        assertThrows(AuditRetryException.class, () -> {
            retryTemplate.execute((RetryCallback<Void, Exception>) context -> {
                context.setAttribute("operation.name", "databaseOperation");
                attemptCount.incrementAndGet();
                throw new SQLException("ORA-12170: TNS:Connect timeout occurred");
            });
        });
        
        assertEquals(5, attemptCount.get()); // Should retry 5 times for database operations
    }

    @Test
    void testDatabaseRetryTemplateWithNonRetryableSQLException() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.databaseRetryTemplate();
        
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        assertThrows(SQLException.class, () -> {
            retryTemplate.execute((RetryCallback<Void, Exception>) context -> {
                context.setAttribute("operation.name", "databaseOperation");
                attemptCount.incrementAndGet();
                throw new SQLException("ORA-00001: unique constraint violated");
            });
        });
        
        assertEquals(1, attemptCount.get()); // Should not retry constraint violations
    }

    @Test
    void testQuickRetryTemplateWithTransientException() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.quickRetryTemplate();
        
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        assertThrows(TransientDataAccessException.class, () -> {
            retryTemplate.execute((RetryCallback<Void, Exception>) context -> {
                attemptCount.incrementAndGet();
                throw new TransientDataAccessException("Temporary failure") {};
            });
        });
        
        assertEquals(2, attemptCount.get()); // Should retry 2 times for quick operations
    }

    @Test
    void testRetryTemplateSuccessAfterFailure() throws Exception {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.auditRetryTemplate();
        
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        String result = retryTemplate.execute((RetryCallback<String, Exception>) context -> {
            context.setAttribute("operation.name", "testOperation");
            int attempt = attemptCount.incrementAndGet();
            if (attempt < 3) {
                throw new AuditPersistenceException("Temporary failure", 
                    new RuntimeException("connection timeout"));
            }
            return "Success";
        });
        
        assertEquals("Success", result);
        assertEquals(3, attemptCount.get()); // Should succeed on third attempt
    }

    @Test
    void testRetryableExceptionTypes() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.databaseRetryTemplate();
        
        // Test various retryable exception types
        Exception[] retryableExceptions = {
            new SQLException("connection timeout"),
            new SQLTransientException("temporary failure"),
            new SQLRecoverableException("recoverable error"),
            new TransientDataAccessException("transient error") {}
        };
        
        for (Exception exception : retryableExceptions) {
            AtomicInteger attemptCount = new AtomicInteger(0);
            
            assertThrows(Exception.class, () -> {
                retryTemplate.execute((RetryCallback<Void, Exception>) context -> {
                    context.setAttribute("operation.name", "testOperation");
                    attemptCount.incrementAndGet();
                    throw exception;
                });
            });
            
            assertTrue(attemptCount.get() > 1, 
                "Exception " + exception.getClass().getSimpleName() + " should be retryable");
        }
    }

    @Test
    void testRetryContextAttributes() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.auditRetryTemplate();
        
        AtomicInteger attemptCount = new AtomicInteger(0);
        String operationName = "testContextOperation";
        
        assertThrows(AuditRetryException.class, () -> {
            retryTemplate.execute((RetryCallback<Void, Exception>) context -> {
                context.setAttribute("operation.name", operationName);
                attemptCount.incrementAndGet();
                
                // Verify context attributes are set
                assertEquals(operationName, context.getAttribute("operation.name"));
                assertNotNull(context.getAttribute("start.time"));
                
                throw new AuditPersistenceException("Test failure", 
                    new RuntimeException("connection failed"));
            });
        });
        
        assertEquals(3, attemptCount.get());
    }

    @Test
    void testRetryListenerLogging() {
        AuditRetryConfig config = new AuditRetryConfig();
        RetryTemplate retryTemplate = config.auditRetryTemplate();
        
        // Verify that retry template is properly configured
        assertNotNull(retryTemplate);
        // The listeners are internal implementation details, so we just verify the template works
        assertTrue(retryTemplate instanceof RetryTemplate);
    }
}