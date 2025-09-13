# Implementation Plan

- [x] 1. Create basic Maven project structure
  - Set up Maven pom.xml with Spring Boot parent
  - Add Spring Boot starter dependencies (web, jdbc, test)
  - Add SpringDoc OpenAPI dependency for Swagger UI
  - _Requirements: 2.1, 6.7_

- [x] 2. Add Oracle database dependencies
  - Add Oracle JDBC driver dependency to pom.xml
  - Add HikariCP connection pool dependency
  - Add Liquibase dependency for database schema management
  - _Requirements: 2.2_

- [x] 3. Create basic Spring Boot application class
  - Create main application class with @SpringBootApplication
  - Add basic application.yml configuration file
  - _Requirements: 2.1_

- [x] 4. Configure Oracle database connection properties
  - Add Oracle database connection URL, username, password to application.yml
  - Configure JdbcTemplate and DataSource properties for Oracle
  - Configure HikariCP connection pool settings for Oracle
  - Configure Liquibase properties for schema management
  - Configure SpringDoc OpenAPI properties for Swagger UI
  - _Requirements: 2.2, 2.5, 6.7_

- [x] 5. Create Oracle local development properties file
  - Create application-local.properties with Oracle connection details
  - Configure connection to localhost:1521/ORCLPDB1 with cm3int schema
  - Set up secure password configuration for local development
  - _Requirements: 2.2, 2.5_

- [x] 6. Create Liquibase changelog structure
  - Create db/changelog/db.changelog-master.xml file
  - Set up Liquibase directory structure for schema migrations
  - Configure changelog file naming conventions
  - _Requirements: 2.2, 2.5_

- [x] 7. Create initial Liquibase changelog for audit table
  - Create db/changelog/001-create-audit-table.xml
  - Define PIPELINE_AUDIT_LOG table structure with all required columns
  - Add proper Oracle data types and constraints
  - Include primary key and foreign key definitions
  - _Requirements: 2.2, 2.3, 8.2_

- [x] 8. Add Liquibase indexes for audit table
  - Create db/changelog/002-create-audit-indexes.xml
  - Add indexes for correlationId, sourceSystem, moduleName, eventTimestamp
  - Optimize indexes for Oracle database performance
  - _Requirements: 2.2, 6.1, 6.2_

- [x] 9. Create AuditStatus enum
  - Define AuditStatus enum with SUCCESS, FAILURE, WARNING values
  - Add unit test for enum values
  - _Requirements: 2.2, 7.5_

- [x] 10. Create CheckpointStage enum
  - Define CheckpointStage enum with all checkpoint values
  - Add unit test for enum values
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [x] 11. Create basic AuditEvent model class
  - Create AuditEvent POJO class without JPA annotations
  - Add auditId field as UUID primary key
  - Add basic constructor, toString, equals, and hashCode methods
  - _Requirements: 2.2, 2.3_

- [x] 12. Add core AuditEvent fields
  - Add correlationId, sourceSystem, moduleName fields
  - Add processName, sourceEntity, destinationEntity fields
  - Add proper getters and setters for each field
  - _Requirements: 2.2, 2.4_

- [x] 13. Add remaining AuditEvent fields
  - Add keyIdentifier, checkpointStage, eventTimestamp fields
  - Add status, message, detailsJson fields
  - Complete all getters and setters
  - _Requirements: 2.2, 2.4_

- [x] 14. Add Builder pattern to AuditEvent
  - Create static Builder inner class for AuditEvent
  - Implement fluent builder methods for all fields
  - Add build() method to construct AuditEvent instances
  - _Requirements: 2.2, 2.5_

- [ ] 15. Create AuditEvent unit tests
  - Write tests for model creation and field validation
  - Test Builder pattern functionality
  - Test equals, hashCode, and toString methods
  - _Requirements: 2.2, 8.2_

- [ ] 16. Create AuditDetails model class
  - Create AuditDetails POJO for JSON metadata
  - Add fields for file metadata (size, hash)
  - Add fields for SQL loader statistics (rows read/loaded/rejected)
  - _Requirements: 2.4, 3.1, 3.2_

- [ ] 17. Add more AuditDetails fields
  - Add fields for record counts and control totals
  - Add fields for business rule input/output data
  - Add proper getters and setters
  - _Requirements: 3.3, 3.4, 4.2, 4.3_

- [ ] 18. Add JSON serialization to AuditDetails
  - Add Jackson annotations for JSON conversion
  - Configure proper JSON include/exclude policies
  - Add builder pattern for easy construction
  - _Requirements: 2.4_

- [ ] 19. Create AuditDetails unit tests
  - Test JSON serialization and deserialization
  - Test builder pattern functionality
  - Test field validation and constraints
  - _Requirements: 2.4_

- [ ] 20. Create basic AuditRepository class with JdbcTemplate
  - Create @Repository class with JdbcTemplate dependency injection
  - Add basic save() method for inserting audit events
  - Add findById() method for retrieving single audit event
  - _Requirements: 2.1, 2.2_

- [ ] 21. Add correlation ID query methods
  - Add findByCorrelationIdOrderByEventTimestamp method using SQL queries
  - Add countByCorrelationId method with JdbcTemplate
  - Implement proper SQL queries with parameter binding
  - _Requirements: 1.2, 6.1_

- [ ] 22. Add source system and module query methods
  - Add findBySourceSystemAndCheckpointStage method with SQL
  - Add findByModuleNameAndStatus method with SQL
  - Implement proper WHERE clause filtering with parameters
  - _Requirements: 1.3, 6.2, 6.3_

- [ ] 23. Add date range and statistics query methods
  - Add findByEventTimestampBetween method with SQL date filtering
  - Add countByCorrelationIdAndStatus method with SQL COUNT query
  - Add pagination support using LIMIT and OFFSET
  - _Requirements: 6.1, 6.4, 5.2_

- [ ] 24. Create AuditRepository integration tests with JdbcTemplate
  - Set up @JdbcTest configuration with Liquibase
  - Write tests for basic CRUD operations using TestJdbcTemplate
  - Test correlation ID and source system queries
  - Verify Liquibase schema creation works correctly
  - _Requirements: 2.1, 2.2, 6.1, 6.2_

- [ ] 25. Test advanced repository query methods
  - Test date range queries and pagination
  - Test count queries and statistics methods
  - Test query performance with sample data
  - _Requirements: 6.3, 6.4, 5.2_

- [ ] 26. Create CorrelationIdManager interface
  - Define interface with generateCorrelationId method
  - Add getCurrentCorrelationId and setCorrelationId methods
  - Add clearCorrelationId method
  - _Requirements: 1.2, 2.3_

- [ ] 27. Implement CorrelationIdManager with ThreadLocal
  - Create implementation using ThreadLocal storage
  - Implement UUID generation with proper formatting
  - Add thread safety and cleanup logic
  - _Requirements: 1.2, 2.3_

- [ ] 28. Create CorrelationIdManager unit tests
  - Test UUID generation and uniqueness
  - Test thread-local storage and isolation
  - Test cleanup and memory leak prevention
  - _Requirements: 1.2, 1.4_

- [ ] 29. Create basic AuditService interface
  - Define AuditService interface with core audit logging methods
  - Add method signatures for all checkpoint logging operations
  - Include proper JavaDoc documentation
  - _Requirements: 2.1, 7.1, 7.2, 7.3, 7.4_

- [ ] 30. Implement basic AuditService with Oracle persistence
  - Create AuditServiceImpl with Oracle database integration
  - Implement core audit event creation and persistence logic
  - Add basic validation and error handling
  - _Requirements: 2.1, 2.2, 2.5, 8.1_

- [ ] 31. Add checkpoint-specific logging methods
  - Implement logFileTransfer method for Checkpoint 1
  - Implement logSqlLoaderOperation method for Checkpoint 2
  - Add proper Oracle transaction management
  - _Requirements: 7.1, 7.2, 4.1, 4.2_

- [ ] 32. Complete remaining checkpoint methods
  - Implement logBusinessRuleApplication method for Checkpoint 3
  - Implement logFileGeneration method for Checkpoint 4
  - Add comprehensive error handling for Oracle operations
  - _Requirements: 7.3, 7.4, 4.3, 4.4_

- [ ] 33. Create AuditService unit tests
  - Write unit tests with mocked Oracle repository
  - Test all checkpoint logging methods
  - Test error handling and validation scenarios
  - _Requirements: 2.1, 2.2, 7.5_

- [ ] 34. Create Swagger configuration
  - Create SwaggerConfig class with @Configuration
  - Configure OpenAPI documentation with title, description, version
  - Set up API servers for development and production
  - Add contact information and API grouping
  - _Requirements: 6.7_

- [ ] 35. Create REST API controller structure with Swagger annotations
  - Create AuditDashboardController class with @RestController
  - Add @RequestMapping for /api/audit base path
  - Add @Tag annotation for Swagger API grouping
  - Set up basic Spring MVC configuration
  - _Requirements: 6.1, 6.6, 6.7_

- [ ] 36. Implement audit events REST endpoint with Swagger documentation
  - Create GET /api/audit/events endpoint with pagination
  - Add @Operation annotation with summary and description
  - Add @Parameter annotations for query parameters
  - Add @ApiResponse annotations for different response codes
  - Return JSON responses with proper HTTP status codes
  - _Requirements: 6.1, 6.2, 6.6, 6.7_

- [ ] 37. Add reconciliation REST endpoints with Swagger documentation
  - Create GET /api/audit/reconciliation/{correlationId} endpoint
  - Implement GET /api/audit/reconciliation/reports with filtering
  - Add @Operation and @Parameter annotations for Swagger
  - Return reconciliation data in JSON format
  - _Requirements: 5.1, 5.2, 5.6, 6.7_

- [ ] 38. Create statistics and discrepancy REST endpoints with Swagger
  - Implement GET /api/audit/statistics endpoint
  - Create GET /api/audit/discrepancies endpoint with filters
  - Add comprehensive Swagger annotations for all parameters
  - Add proper error handling and HTTP status codes
  - _Requirements: 6.4, 5.4, 6.6, 6.7_

- [ ] 39. Add REST API request/response DTOs with Swagger schemas
  - Create AuditEventDTO for API responses
  - Create ReconciliationReportDTO for report endpoints
  - Add @Schema annotations for Swagger documentation
  - Add proper JSON serialization annotations
  - _Requirements: 6.1, 5.6, 6.6, 6.7_

- [ ] 40. Create REST API integration tests including Swagger
  - Write @WebMvcTest tests for all endpoints
  - Test JSON request/response serialization
  - Test pagination, filtering, and error scenarios
  - Verify Swagger UI accessibility and documentation
  - _Requirements: 6.1, 6.2, 6.4, 6.6, 6.7_

- [ ] 41. Add Oracle-specific configuration
  - Configure Oracle DataSource and connection pool settings
  - Add Oracle-specific JdbcTemplate properties for performance
  - Configure transaction management for Oracle with JdbcTemplate
  - Configure Liquibase for Oracle schema management
  - _Requirements: 2.2, 2.5_

- [ ] 42. Create database integration tests with Oracle and Liquibase
  - Set up @JdbcTest with Oracle test configuration
  - Test all repository methods with Oracle database using JdbcTemplate
  - Verify Liquibase schema creation and migrations
  - Test Oracle-specific features and SQL queries
  - _Requirements: 2.2, 2.5, 6.1, 6.2_

- [ ] 43. Implement comprehensive error handling
  - Create audit-specific exception classes
  - Add global exception handler for REST API with Swagger error documentation
  - Implement retry logic for Oracle connection issues with JdbcTemplate
  - _Requirements: 7.5, 8.4, 6.6, 6.7_

- [ ] 44. Add API security configuration
  - Configure Spring Security for REST endpoints
  - Add authentication and authorization for audit APIs
  - Implement role-based access control
  - Configure Swagger UI security for protected endpoints
  - _Requirements: 8.3, 6.1, 6.7_

- [ ] 45. Create end-to-end integration tests
  - Test complete audit flow from service to Oracle database using JdbcTemplate
  - Test REST API endpoints with real Oracle database
  - Verify correlation ID propagation across all components
  - Test Liquibase migrations in integration environment
  - Verify Swagger UI functionality and API documentation
  - _Requirements: 1.1, 1.2, 2.5, 6.6, 6.7_