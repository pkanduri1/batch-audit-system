package com.company.audit.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Oracle-specific DataSource configuration optimized for Spring Boot 3.4+ and Java 17+.
 * Provides comprehensive Oracle database connectivity with HikariCP connection pooling,
 * performance-tuned JdbcTemplate, and transaction management.
 */
@Configuration
@EnableTransactionManagement
public class OracleDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${audit.database.connection-pool-size:10}")
    private int maxPoolSize;

    @Value("${audit.database.batch-size:100}")
    private int batchSize;

    /**
     * Configure Oracle DataSource with HikariCP connection pool optimized for Oracle database.
     * Includes Oracle-specific connection properties and performance tuning.
     * 
     * @return HikariDataSource configured for Oracle with optimal settings
     */
    @Bean
    @Primary
    public DataSource oracleDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection properties
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Connection pool configuration optimized for Oracle
        config.setPoolName("OracleAuditHikariPool");
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(Math.max(2, maxPoolSize / 4));
        config.setConnectionTimeout(20000); // 20 seconds
        config.setIdleTimeout(300000); // 5 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setLeakDetectionThreshold(60000); // 1 minute
        config.setValidationTimeout(5000); // 5 seconds
        
        // Oracle-specific connection test query
        config.setConnectionTestQuery("SELECT 1 FROM DUAL");
        
        // Disable auto-commit for better transaction control
        config.setAutoCommit(false);
        
        // Oracle-specific DataSource properties for performance optimization
        Properties dataSourceProperties = new Properties();
        
        // Oracle connection properties for better performance
        dataSourceProperties.setProperty("oracle.jdbc.ReadTimeout", "60000");
        dataSourceProperties.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        dataSourceProperties.setProperty("oracle.jdbc.implicitStatementCacheSize", "25");
        dataSourceProperties.setProperty("oracle.jdbc.explicitStatementCacheSize", "25");
        
        // Enable Oracle statement caching for better performance
        dataSourceProperties.setProperty("oracle.jdbc.enableQueryResultCache", "true");
        dataSourceProperties.setProperty("oracle.jdbc.queryResultCacheSize", "100");
        
        // Oracle network optimization
        dataSourceProperties.setProperty("oracle.net.disableOob", "true");
        dataSourceProperties.setProperty("oracle.jdbc.J2EE13Compliant", "true");
        
        // Set timezone to UTC for consistency
        dataSourceProperties.setProperty("oracle.jdbc.timezoneAsRegion", "false");
        dataSourceProperties.setProperty("user.timezone", "UTC");
        
        config.setDataSourceProperties(dataSourceProperties);
        
        return new HikariDataSource(config);
    }

    /**
     * Configure JdbcTemplate with Oracle-specific optimizations for audit operations.
     * Includes performance tuning for batch operations and large result sets.
     * 
     * @param dataSource the Oracle DataSource
     * @return JdbcTemplate optimized for Oracle audit operations
     */
    @Bean
    @Primary
    public JdbcTemplate oracleJdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Oracle-specific JdbcTemplate configuration for performance
        jdbcTemplate.setFetchSize(batchSize); // Optimize fetch size for audit queries
        jdbcTemplate.setMaxRows(0); // No limit on max rows
        jdbcTemplate.setQueryTimeout(30); // 30 second query timeout
        
        // Enable result set streaming for large audit datasets
        jdbcTemplate.setResultsMapCaseInsensitive(true);
        
        return jdbcTemplate;
    }

    /**
     * Configure transaction manager for Oracle database operations.
     * Provides proper transaction management for audit event persistence.
     * 
     * @param dataSource the Oracle DataSource
     * @return PlatformTransactionManager for Oracle transactions
     */
    @Bean
    @Primary
    public PlatformTransactionManager oracleTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        
        // Configure transaction timeout (5 minutes for audit operations)
        transactionManager.setDefaultTimeout(300);
        
        // Enable nested transactions for complex audit scenarios
        transactionManager.setNestedTransactionAllowed(true);
        
        // Validate existing transactions
        transactionManager.setValidateExistingTransaction(true);
        
        return transactionManager;
    }
}