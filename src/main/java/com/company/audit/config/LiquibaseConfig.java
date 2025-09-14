package com.company.audit.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Liquibase configuration for Oracle schema management compatible with Spring Boot 3.4+ and Java 17+.
 * Provides comprehensive database migration and schema versioning for the audit system.
 */
@Configuration
public class LiquibaseConfig {

    @Value("${spring.liquibase.change-log:classpath:db/changelog/db.changelog-master.xml}")
    private String changeLog;

    @Value("${spring.liquibase.contexts:${spring.profiles.active}}")
    private String contexts;

    @Value("${spring.liquibase.default-schema:${spring.datasource.username}}")
    private String defaultSchema;

    @Value("${spring.liquibase.liquibase-schema:${spring.datasource.username}}")
    private String liquibaseSchema;

    @Value("${spring.liquibase.database-change-log-table:DATABASECHANGELOG}")
    private String changeLogTable;

    @Value("${spring.liquibase.database-change-log-lock-table:DATABASECHANGELOGLOCK}")
    private String changeLogLockTable;

    @Value("${spring.liquibase.enabled:true}")
    private boolean enabled;

    @Value("${spring.liquibase.drop-first:false}")
    private boolean dropFirst;

    /**
     * Configure SpringLiquibase for Oracle database schema management.
     * Optimized for Oracle-specific features and Spring Boot 3.4+ integration.
     * 
     * @param dataSource the Oracle DataSource
     * @return SpringLiquibase configured for Oracle audit schema management
     */
    @Bean
    @DependsOn("oracleDataSource")
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        
        // Basic Liquibase configuration
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setContexts(contexts);
        liquibase.setDefaultSchema(defaultSchema);
        liquibase.setLiquibaseSchema(liquibaseSchema);
        liquibase.setDatabaseChangeLogTable(changeLogTable);
        liquibase.setDatabaseChangeLogLockTable(changeLogLockTable);
        
        // Control flags
        liquibase.setShouldRun(enabled);
        liquibase.setDropFirst(dropFirst);
        
        // Oracle-specific Liquibase parameters for better performance
        // Note: In newer Liquibase versions, parameters are set via application properties
        // These parameters will be available in changelog files via ${parameter.name} syntax
        
        // Set additional properties for Oracle optimization
        System.setProperty("liquibase.audit.table.tablespace", "USERS");
        System.setProperty("liquibase.audit.index.tablespace", "USERS");
        System.setProperty("liquibase.oracle.batch.size", "1000");
        System.setProperty("liquibase.schema.name", defaultSchema);
        System.setProperty("liquibase.audit.schema", defaultSchema);
        System.setProperty("liquibase.database.type", "oracle");
        System.setProperty("liquibase.database.version", "19c");
        System.setProperty("liquibase.oracle.parallel.degree", "4");
        System.setProperty("liquibase.oracle.logging", "NOLOGGING");
        
        return liquibase;
    }
}