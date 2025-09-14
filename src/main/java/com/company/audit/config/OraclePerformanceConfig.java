package com.company.audit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Oracle performance optimization configuration for Spring Boot 3.4+ and Java 17+.
 * Provides advanced Oracle-specific performance tuning, retry mechanisms, and batch processing optimizations.
 */
@Configuration
@EnableRetry
@ConfigurationProperties(prefix = "audit.oracle.performance")
public class OraclePerformanceConfig {

    @Value("${audit.database.batch-size:100}")
    private int batchSize;

    @Value("${audit.oracle.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${audit.oracle.retry.initial-delay:1000}")
    private long initialRetryDelay;

    @Value("${audit.oracle.retry.max-delay:10000}")
    private long maxRetryDelay;

    @Value("${audit.oracle.retry.multiplier:2.0}")
    private double retryMultiplier;

    /**
     * Configure NamedParameterJdbcTemplate for complex Oracle queries with named parameters.
     * Optimized for audit system's complex filtering and reporting queries.
     * 
     * @param dataSource the Oracle DataSource
     * @return NamedParameterJdbcTemplate optimized for Oracle audit operations
     */
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        
        // Configure for optimal Oracle performance
        template.getJdbcTemplate().setFetchSize(batchSize);
        template.getJdbcTemplate().setMaxRows(0);
        template.getJdbcTemplate().setQueryTimeout(60); // 1 minute for complex queries
        
        return template;
    }

    /**
     * Configure SimpleJdbcInsert for high-performance batch inserts into Oracle audit table.
     * Optimized for bulk audit event insertion with Oracle-specific performance features.
     * 
     * @param dataSource the Oracle DataSource
     * @return SimpleJdbcInsert configured for Oracle audit table batch operations
     */
    @Bean
    public SimpleJdbcInsert auditEventJdbcInsert(DataSource dataSource) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("PIPELINE_AUDIT_LOG")
                .usingGeneratedKeyColumns("AUDIT_ID")
                .usingColumns(
                    "CORRELATION_ID", "SOURCE_SYSTEM", "MODULE_NAME", "PROCESS_NAME",
                    "SOURCE_ENTITY", "DESTINATION_ENTITY", "KEY_IDENTIFIER", 
                    "CHECKPOINT_STAGE", "EVENT_TIMESTAMP", "STATUS", "MESSAGE", "DETAILS_JSON"
                );
    }

    /**
     * Configure RetryTemplate for Oracle database operations with exponential backoff.
     * Handles transient Oracle connection issues and deadlocks gracefully.
     * 
     * @return RetryTemplate configured for Oracle-specific retry scenarios
     */
    @Bean
    public RetryTemplate oracleRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Configure retry policy for Oracle-specific exceptions
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(org.springframework.dao.TransientDataAccessException.class, true);
        retryableExceptions.put(org.springframework.dao.RecoverableDataAccessException.class, true);
        retryableExceptions.put(org.springframework.dao.DataAccessResourceFailureException.class, true);
        retryableExceptions.put(java.sql.SQLRecoverableException.class, true);
        retryableExceptions.put(java.sql.SQLTransientException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxRetryAttempts, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Configure exponential backoff for Oracle connection recovery
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialRetryDelay);
        backOffPolicy.setMaxInterval(maxRetryDelay);
        backOffPolicy.setMultiplier(retryMultiplier);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }

    /**
     * Oracle-specific configuration properties for performance tuning.
     * 
     * @return Map of Oracle performance configuration properties
     */
    @Bean
    public Map<String, Object> oraclePerformanceProperties() {
        Map<String, Object> properties = new HashMap<>();
        
        // Batch processing configuration
        properties.put("oracle.batch.size", batchSize);
        properties.put("oracle.fetch.size", batchSize);
        
        // Connection and statement caching
        properties.put("oracle.statement.cache.size", 25);
        properties.put("oracle.implicit.cache.enabled", true);
        properties.put("oracle.explicit.cache.enabled", true);
        
        // Oracle-specific performance hints
        properties.put("oracle.query.hints.enabled", true);
        properties.put("oracle.parallel.enabled", true);
        properties.put("oracle.parallel.degree", 4);
        
        // Memory and resource management
        properties.put("oracle.lob.prefetch.enabled", true);
        properties.put("oracle.row.prefetch", batchSize);
        properties.put("oracle.default.row.prefetch", batchSize);
        
        // Transaction and locking optimization
        properties.put("oracle.transaction.isolation", "READ_COMMITTED");
        properties.put("oracle.lock.timeout", 30);
        
        return properties;
    }

    // Getters and setters for configuration properties
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public long getInitialRetryDelay() {
        return initialRetryDelay;
    }

    public void setInitialRetryDelay(long initialRetryDelay) {
        this.initialRetryDelay = initialRetryDelay;
    }

    public long getMaxRetryDelay() {
        return maxRetryDelay;
    }

    public void setMaxRetryDelay(long maxRetryDelay) {
        this.maxRetryDelay = maxRetryDelay;
    }

    public double getRetryMultiplier() {
        return retryMultiplier;
    }

    public void setRetryMultiplier(double retryMultiplier) {
        this.retryMultiplier = retryMultiplier;
    }
}