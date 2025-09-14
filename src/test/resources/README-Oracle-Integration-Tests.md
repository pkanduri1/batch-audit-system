# Oracle Database Integration Tests

This document describes the Oracle database integration tests for the Batch Audit System, designed for Spring Boot 3.4+ and Java 17+.

## Overview

The Oracle integration tests verify:
- Oracle database connectivity and configuration
- Liquibase schema creation and migrations
- JdbcTemplate operations with Oracle-specific features
- Performance with Oracle indexes and query optimization
- Oracle-specific SQL features using Java 17+ text blocks
- Transaction management with Oracle database

## Test Structure

### Test Classes

1. **AuditRepositoryOracleIntegrationTest**
   - Comprehensive Oracle database integration tests
   - Uses Test_ prefixed tables for isolation
   - Tests all repository methods with Oracle database
   - Verifies Oracle-specific features (CLOB, timestamps, functions)

2. **OracleIntegrationConfigTest**
   - Oracle configuration and connectivity tests
   - Verifies HikariCP connection pool setup
   - Tests JdbcTemplate Oracle optimizations
   - Validates Liquibase integration with Oracle

### Test Configuration Files

1. **application-integration.yml**
   - Oracle-specific test configuration
   - Connection pool settings optimized for testing
   - Liquibase configuration for test schema management

2. **test-changelog-master.xml**
   - Test-specific Liquibase changelog
   - Creates Test_ prefixed tables for isolation
   - Oracle-optimized table and index creation

## Prerequisites

### Oracle Database Setup

1. **Oracle Database Instance**
   - Oracle 19c or 21c recommended
   - Test database/schema with appropriate permissions
   - Default connection: `localhost:1521/ORCLPDB1`

2. **Database User Permissions**
   ```sql
   -- Required permissions for test user
   GRANT CREATE SESSION TO cm3int_test;
   GRANT CREATE TABLE TO cm3int_test;
   GRANT CREATE INDEX TO cm3int_test;
   GRANT CREATE SEQUENCE TO cm3int_test;
   GRANT UNLIMITED TABLESPACE TO cm3int_test;
   ```

3. **Environment Variables**
   ```bash
   export ORACLE_TEST_DB_URL="jdbc:oracle:thin:@//localhost:1521/ORCLPDB1"
   export ORACLE_TEST_DB_USERNAME="cm3int_test"
   export ORACLE_TEST_DB_PASSWORD="testpassword"
   ```

### Maven Dependencies

The following Oracle-specific dependencies are required:
- Oracle JDBC Driver (ojdbc11)
- HikariCP Connection Pool
- Liquibase for Oracle
- Spring Boot Test Framework

## Running the Tests

### Comprehensive Database Integration Tests (Recommended)

The new `ComprehensiveDatabaseIntegrationTest` class provides the most complete testing coverage and works with both Oracle and H2 databases:

```bash
# Run comprehensive database integration tests (works with H2 by default)
mvn test -Dtest=ComprehensiveDatabaseIntegrationTest

# Run with Oracle database when available
mvn test -Dtest=ComprehensiveDatabaseIntegrationTest \
  -Dspring.profiles.active=integration \
  -DORACLE_TEST_DB_URL="jdbc:oracle:thin:@//localhost:1521/ORCLPDB1" \
  -DORACLE_TEST_DB_USERNAME="cm3int_test" \
  -DORACLE_TEST_DB_PASSWORD="testpassword"
```

### Oracle-Specific Integration Tests (Requires Oracle Database)

The original Oracle-specific tests require an actual Oracle database connection:

```bash
# Run Oracle repository integration tests (requires Oracle database)
mvn test -Dtest=AuditRepositoryOracleIntegrationTest \
  -Dspring.profiles.active=integration \
  -Dtest.database.integration=true

# Run Oracle configuration tests
mvn test -Dtest=OracleIntegrationConfigTest -Dspring.profiles.active=integration
```

## Running the Tests

### Individual Test Classes

```bash
# Run Oracle repository integration tests
mvn test -Dtest=AuditRepositoryOracleIntegrationTest -Dspring.profiles.active=integration

# Run Oracle configuration tests
mvn test -Dtest=OracleIntegrationConfigTest -Dspring.profiles.active=integration
```

### All Integration Tests

```bash
# Run all integration tests with Oracle profile
mvn test -Dspring.profiles.active=integration -Dtest="*Integration*"
```

### With Custom Oracle Connection

```bash
# Run tests with custom Oracle connection
mvn test -Dtest=AuditRepositoryOracleIntegrationTest \
  -Dspring.profiles.active=integration \
  -DORACLE_TEST_DB_URL="jdbc:oracle:thin:@//your-oracle-host:1521/YOUR_SERVICE" \
  -DORACLE_TEST_DB_USERNAME="your_test_user" \
  -DORACLE_TEST_DB_PASSWORD="your_test_password"
```

## Test Features

### Oracle-Specific Features Tested

1. **Oracle Data Types**
   - VARCHAR2 for string fields
   - CLOB for large JSON data
   - TIMESTAMP for date/time fields
   - Oracle constraints and check constraints

2. **Oracle Functions**
   - `DUAL` table for testing
   - `SYSDATE` and `CURRENT_TIMESTAMP`
   - `SYS_GUID()` for UUID generation
   - `SYS_CONTEXT()` for session information

3. **Oracle Performance Features**
   - Connection pooling with HikariCP
   - Statement caching
   - Fetch size optimization
   - Index-based query performance

4. **Oracle SQL Features**
   - ROW_NUMBER() for pagination
   - Oracle-specific pagination syntax
   - Complex WHERE clauses with Oracle functions
   - CLOB handling for large JSON data

### Java 17+ Features Used

1. **Text Blocks**
   ```java
   String createTableSql = """
       CREATE TABLE Test_PIPELINE_AUDIT_LOG (
           AUDIT_ID VARCHAR2(36) PRIMARY KEY,
           CORRELATION_ID VARCHAR2(36) NOT NULL,
           -- ... other columns
       ) TABLESPACE USERS
       """;
   ```

2. **Enhanced Switch Expressions**
   ```java
   AuditStatus status = switch (j % 3) {
       case 0 -> AuditStatus.SUCCESS;
       case 1 -> AuditStatus.FAILURE;
       default -> AuditStatus.WARNING;
   };
   ```

3. **Pattern Matching and Records**
   - Used where applicable for data transfer objects
   - Enhanced instanceof checks

## Test Data Management

### Test Table Isolation

All integration tests use `Test_` prefixed tables:
- `Test_PIPELINE_AUDIT_LOG` instead of `PIPELINE_AUDIT_LOG`
- `Test_IDX_*` for indexes
- `Test_CHK_*` for constraints

This ensures complete isolation from production data.

### Test Data Cleanup

- Each test method creates and cleans up its own test data
- `@BeforeEach` creates test table structure
- `@AfterEach` cleans up test data
- Liquibase manages schema versioning for tests

## Performance Testing

### Query Performance Tests

The integration tests include performance verification:
- Correlation ID queries should complete within 1 second
- Date range queries should complete within 2 seconds
- Complex multi-column queries should complete within 3 seconds
- Pagination queries should handle large datasets efficiently

### Connection Pool Testing

- Tests multiple concurrent connections
- Verifies connection recovery and pooling
- Tests connection validation and timeout handling

## Troubleshooting

### Common Issues

1. **Oracle Connection Failures**
   - Verify Oracle database is running
   - Check connection URL, username, and password
   - Ensure network connectivity to Oracle instance

2. **Permission Errors**
   - Verify test user has required database permissions
   - Check tablespace access and quotas
   - Ensure CREATE TABLE and INDEX permissions

3. **Liquibase Issues**
   - Check Liquibase changelog syntax
   - Verify Oracle-specific SQL compatibility
   - Review Liquibase context configuration

### Debug Configuration

Enable debug logging for troubleshooting:
```yaml
logging:
  level:
    com.company.audit: DEBUG
    org.springframework.jdbc: DEBUG
    liquibase: DEBUG
    com.zaxxer.hikari: DEBUG
    oracle.jdbc: INFO
```

## Test Coverage

The comprehensive database integration tests cover:
- ✅ Database connectivity (Oracle when available, H2 as fallback)
- ✅ HikariCP connection pool configuration
- ✅ JdbcTemplate database optimizations with Spring Boot 3.4+
- ✅ Liquibase schema management (when enabled)
- ✅ Database-specific data types (VARCHAR2/VARCHAR, CLOB, TIMESTAMP)
- ✅ Database functions (Oracle: DUAL, SYSDATE, SYS_GUID; H2: RANDOM_UUID)
- ✅ Database performance features (indexes, pagination)
- ✅ Transaction management with Spring Boot 3.4+
- ✅ Error handling and recovery scenarios
- ✅ Large dataset performance testing (100+ records)
- ✅ Concurrent operation testing with thread safety
- ✅ Comprehensive repository method testing
- ✅ Java 17+ language features (text blocks, switch expressions, pattern matching)
- ✅ Test_ prefixed table isolation for safe testing
- ✅ CRUD operations with all repository methods
- ✅ Query performance with indexed searches
- ✅ CLOB handling for large JSON data
- ✅ Statistics and count queries
- ✅ Date range and timestamp queries
- ✅ Pagination with database-appropriate syntax

## Integration with CI/CD

For continuous integration, consider:
1. Oracle Docker containers for consistent test environments
2. Test database provisioning scripts
3. Environment-specific configuration management
4. Performance benchmarking and regression testing

## Best Practices

1. **Test Isolation**: Always use Test_ prefixed tables
2. **Data Cleanup**: Ensure proper cleanup after each test
3. **Performance Monitoring**: Include performance assertions
4. **Error Handling**: Test both success and failure scenarios
5. **Oracle Features**: Leverage Oracle-specific optimizations
6. **Documentation**: Keep test documentation up to date