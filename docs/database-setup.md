# Database Setup and Schema Management

This document provides detailed instructions for setting up the Oracle database and managing schema changes using Liquibase.

## Oracle Database Requirements

### Supported Versions
- Oracle Database 19c (recommended)
- Oracle Database 21c

### Required Privileges
The database user needs the following privileges:
```sql
-- Basic table operations
GRANT CREATE TABLE TO cm3int;
GRANT CREATE INDEX TO cm3int;
GRANT CREATE SEQUENCE TO cm3int;

-- Liquibase operations
GRANT CREATE VIEW TO cm3int;
GRANT ALTER ANY TABLE TO cm3int;
GRANT DROP ANY TABLE TO cm3int;

-- Connection and session management
GRANT CREATE SESSION TO cm3int;
GRANT UNLIMITED TABLESPACE TO cm3int;
```

## Liquibase Configuration

### Master Changelog Structure

The `db.changelog-master.xml` file serves as the entry point for all database changes:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.0.xsd">

    <!-- Initial audit table creation -->
    <include file="db/changelog/001-create-audit-table.xml"/>
    
    <!-- Audit table indexes for performance -->
    <include file="db/changelog/002-create-audit-indexes.xml"/>
    
</databaseChangeLog>
```

### Changelog File Naming Convention

- **Format**: `{sequence}-{description}.xml`
- **Sequence**: 3-digit number (001, 002, 003, etc.)
- **Description**: Kebab-case description of the change
- **Examples**:
  - `001-create-audit-table.xml`
  - `002-create-audit-indexes.xml`
  - `003-add-retention-policy.xml`

## Database Schema

### PIPELINE_AUDIT_LOG Table

The main audit table structure:

```sql
CREATE TABLE PIPELINE_AUDIT_LOG (
    AUDIT_ID RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    CORRELATION_ID RAW(16) NOT NULL,
    SOURCE_SYSTEM VARCHAR2(50) NOT NULL,
    MODULE_NAME VARCHAR2(100),
    PROCESS_NAME VARCHAR2(50) NOT NULL,
    SOURCE_ENTITY VARCHAR2(255),
    DESTINATION_ENTITY VARCHAR2(255),
    KEY_IDENTIFIER VARCHAR2(100),
    CHECKPOINT_STAGE VARCHAR2(50) NOT NULL,
    EVENT_TIMESTAMP TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    STATUS VARCHAR2(20) NOT NULL CHECK (STATUS IN ('SUCCESS', 'FAILURE', 'WARNING')),
    MESSAGE VARCHAR2(1000) NOT NULL,
    DETAILS_JSON CLOB CHECK (DETAILS_JSON IS JSON)
);
```

### Indexes for Performance

```sql
-- Correlation ID lookup (most common query pattern)
CREATE INDEX IDX_AUDIT_CORRELATION_ID ON PIPELINE_AUDIT_LOG(CORRELATION_ID);

-- Source system filtering
CREATE INDEX IDX_AUDIT_SOURCE_SYSTEM ON PIPELINE_AUDIT_LOG(SOURCE_SYSTEM);

-- Module name filtering
CREATE INDEX IDX_AUDIT_MODULE_NAME ON PIPELINE_AUDIT_LOG(MODULE_NAME);

-- Time-based queries
CREATE INDEX IDX_AUDIT_EVENT_TIMESTAMP ON PIPELINE_AUDIT_LOG(EVENT_TIMESTAMP);

-- Status filtering for error analysis
CREATE INDEX IDX_AUDIT_STATUS ON PIPELINE_AUDIT_LOG(STATUS);

-- Composite index for common filter combinations
CREATE INDEX IDX_AUDIT_COMPOSITE ON PIPELINE_AUDIT_LOG(SOURCE_SYSTEM, STATUS, EVENT_TIMESTAMP);
```

## Liquibase Operations

### Initial Setup

1. **Validate Configuration**
   ```bash
   mvn liquibase:validate
   ```

2. **Check Status**
   ```bash
   mvn liquibase:status
   ```

3. **Run Initial Migration**
   ```bash
   mvn liquibase:update
   ```

### Development Workflow

1. **Create New Changelog**
   ```bash
   # Create new file: src/main/resources/db/changelog/003-new-feature.xml
   ```

2. **Add to Master Changelog**
   ```xml
   <include file="db/changelog/003-new-feature.xml"/>
   ```

3. **Test Migration**
   ```bash
   mvn liquibase:update
   ```

4. **Validate Changes**
   ```bash
   mvn liquibase:validate
   mvn test
   ```

### Production Deployment

1. **Generate SQL for Review**
   ```bash
   mvn liquibase:updateSQL > migration-preview.sql
   ```

2. **Apply Changes**
   ```bash
   mvn liquibase:update -Dspring.profiles.active=prod
   ```

3. **Verify Deployment**
   ```bash
   mvn liquibase:status -Dspring.profiles.active=prod
   ```

### Rollback Procedures

1. **Rollback Last Change**
   ```bash
   mvn liquibase:rollback -Dliquibase.rollbackCount=1
   ```

2. **Rollback to Specific Tag**
   ```bash
   mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0.0
   ```

3. **Rollback to Date**
   ```bash
   mvn liquibase:rollback -Dliquibase.rollbackDate=2024-01-01
   ```

## Environment-Specific Configuration

### Development Environment
```yaml
spring:
  liquibase:
    contexts: dev
    drop-first: false
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml
```

### Production Environment
```yaml
spring:
  liquibase:
    contexts: prod
    drop-first: false
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml
```

### Test Environment
```yaml
spring:
  liquibase:
    contexts: test
    drop-first: true  # Clean slate for each test run
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml
```

## Best Practices

### Changelog Development
1. **Never modify existing changelogs** - always create new ones
2. **Use meaningful IDs** for each changeset
3. **Include rollback instructions** where possible
4. **Test changes locally** before committing
5. **Use contexts** to separate environment-specific changes

### Performance Considerations
1. **Create indexes after data loading** for better performance
2. **Use batch operations** for large data migrations
3. **Monitor execution time** for complex changes
4. **Consider maintenance windows** for production deployments

### Security Guidelines
1. **Never include passwords** in changelog files
2. **Use environment variables** for sensitive configuration
3. **Limit database privileges** to minimum required
4. **Audit database changes** in production environments

## Troubleshooting

### Common Issues

1. **Liquibase Lock Issues**
   ```bash
   mvn liquibase:releaseLocks
   ```

2. **Checksum Validation Failures**
   ```bash
   mvn liquibase:clearCheckSums
   mvn liquibase:changelogSync
   ```

3. **Oracle Connection Issues**
   - Verify Oracle JDBC driver version compatibility
   - Check network connectivity and firewall settings
   - Validate database credentials and privileges

### Logging and Monitoring

Enable detailed Liquibase logging:
```yaml
logging:
  level:
    liquibase: DEBUG
    org.springframework.jdbc: DEBUG
```

Monitor database changes:
```sql
-- Check Liquibase history
SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC;

-- Check current locks
SELECT * FROM DATABASECHANGELOGLOCK;
```