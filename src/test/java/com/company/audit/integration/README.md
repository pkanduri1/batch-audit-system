# End-to-End Integration Tests

This directory contains comprehensive end-to-end integration tests for the Batch Audit System that verify complete system functionality from service layer to Oracle database.

## Test Coverage

### 1. EndToEndAuditIntegrationTest
- **Purpose**: Tests complete audit flow from service to Oracle database using JdbcTemplate with Spring Boot 3.4+ and Java 17+
- **Coverage**:
  - Complete audit flow through all 4 checkpoints (File Transfer, SQL Loader, Business Rules, File Generation)
  - REST API endpoints with real database operations
  - Correlation ID propagation across all components
  - Error handling and resilience scenarios
- **Requirements**: 1.1, 1.2, 2.5, 6.6, 6.7

### 2. RestApiDatabaseIntegrationTest
- **Purpose**: Tests REST API endpoints with real Oracle database using Spring Boot 3.4+ test framework
- **Coverage**:
  - Audit events endpoint with pagination and filtering
  - Reconciliation endpoints with database queries
  - Statistics endpoint with database aggregations
  - Discrepancies endpoint with filtering
  - API error handling with database constraints
  - Database performance testing with large datasets
- **Requirements**: 6.6, 6.7, 2.5

### 3. CorrelationIdPropagationIntegrationTest
- **Purpose**: Tests correlation ID propagation across all components with Java 17+ enhanced debugging features
- **Coverage**:
  - Correlation ID propagation through complete audit flow
  - Thread isolation and concurrent processing
  - Persistence across service method calls
  - Cleanup and memory leak prevention
  - Generation uniqueness and format validation
  - Database transaction consistency
  - Error scenario handling
- **Requirements**: 1.2

### 4. LiquibaseMigrationIntegrationTest
- **Purpose**: Tests Liquibase migrations in integration environment with Spring Boot 3.4+ Liquibase integration
- **Coverage**:
  - Table creation with Test_ prefix
  - Database schema structure validation
  - Index creation for performance optimization
  - Constraint validation
  - Changelog execution history
  - Validation and rollback capabilities
  - Database performance with created indexes
- **Requirements**: 2.5

### 5. SwaggerOpenApiIntegrationTest
- **Purpose**: Tests SpringDoc OpenAPI v2 Swagger UI functionality and API documentation generation
- **Coverage**:
  - Swagger UI accessibility and basic functionality
  - OpenAPI 3.0 JSON documentation generation
  - API endpoints documentation
  - Schema and components documentation
  - Parameter documentation and validation
  - Response documentation and status codes
  - Tags and grouping functionality
  - Configuration and customization
- **Requirements**: 6.7

## Configuration

### Integration Test Profile
- **Profile**: `integration`
- **Configuration File**: `src/test/resources/application-integration.yml`
- **Database**: Oracle with Test_ table prefixes
- **Features**:
  - SpringDoc OpenAPI v2 enabled
  - Debug logging for audit components
  - Test-specific configuration overrides

### Test Database Setup
- Uses Test_ prefixed tables (e.g., Test_PIPELINE_AUDIT_LOG)
- Liquibase migrations with integration context
- Oracle-specific configurations optimized for testing
- Connection pooling configured for test environment

## Key Features Tested

### Spring Boot 3.4+ Integration
- Auto-configuration with Spring Boot 3.4+
- JdbcTemplate with Oracle database integration
- Test framework enhancements
- Liquibase integration
- SpringDoc OpenAPI v2 integration

### Java 17+ Features
- Enhanced debugging capabilities
- Text blocks for SQL readability
- Pattern matching and records where applicable
- Virtual threads compatibility for future versions
- UUID improvements and enhanced features

### Oracle Database Integration
- JdbcTemplate with Oracle-specific SQL operations
- Performance optimization with proper indexing
- Transaction management
- Connection pooling with HikariCP
- Schema validation and constraints

### REST API Testing
- MockMvc integration testing
- JSON serialization/deserialization
- Pagination and filtering
- Error handling and status codes
- Swagger UI and OpenAPI documentation

## Running the Tests

### Prerequisites
- Oracle database running on localhost:1521/ORCLPDB1
- Schema: cm3int with appropriate permissions
- Environment variable: ORACLE_PASSWORD (optional, defaults to cm3int)

### Execution
```bash
# Run all integration tests
mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=integration

# Run specific integration test
mvn test -Dtest=EndToEndAuditIntegrationTest -Dspring.profiles.active=integration

# Run with debug logging
mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=integration -Dlogging.level.com.company.audit=DEBUG
```

### Test Data Management
- Tests use Test_ prefixed tables to avoid conflicts
- Automatic cleanup in @BeforeEach methods
- Isolated test data for each test method
- Performance test data generation for load testing

## Implementation Notes

### Method Signatures
All integration tests use the correct AuditService method signatures with full parameter lists:
- `logFileTransfer(UUID correlationId, String sourceSystem, String fileName, String processName, String sourceEntity, String destinationEntity, String keyIdentifier, AuditStatus status, String message, AuditDetails auditDetails)`
- `logSqlLoaderOperation(...)` with similar signature
- `logBusinessRuleApplication(...)` with similar signature
- `logFileGeneration(...)` with similar signature

### Error Handling
- Graceful handling of null parameters
- Database connection error scenarios
- Correlation ID management errors
- API validation errors
- Liquibase migration failures

### Performance Considerations
- Database query optimization testing
- Index performance validation
- Connection pool efficiency
- Memory usage monitoring
- Response time validation

## Requirements Mapping

| Requirement | Test Coverage |
|-------------|---------------|
| 1.1 | EndToEndAuditIntegrationTest - Complete audit flow |
| 1.2 | CorrelationIdPropagationIntegrationTest - Correlation ID management |
| 2.5 | LiquibaseMigrationIntegrationTest, RestApiDatabaseIntegrationTest - Database integration |
| 6.6 | RestApiDatabaseIntegrationTest, EndToEndAuditIntegrationTest - REST API functionality |
| 6.7 | SwaggerOpenApiIntegrationTest - API documentation and Swagger UI |

## Future Enhancements

### Additional Test Scenarios
- Multi-threaded concurrent processing
- Large dataset performance testing
- Network failure simulation
- Database failover testing
- Security integration testing

### Test Infrastructure
- Test containers for Oracle database
- Automated test data generation
- Performance benchmarking
- Test result reporting and metrics
- Continuous integration pipeline integration