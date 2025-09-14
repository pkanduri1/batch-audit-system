# Implementation Plan

- [x] 1. Create basic Maven project structure
  - Set up Maven pom.xml with Spring Boot 3.4+ parent and Java 17+ target
  - Add Spring Boot starter dependencies (web, jdbc, test) compatible with Spring Boot 3.4+
  - Add SpringDoc OpenAPI v2 dependency for Swagger UI (compatible with Spring Boot 3.x)
  - Configure Maven compiler plugin for Java 17+ source and target versions
  - _Requirements: 2.1, 6.7_

- [x] 2. Add Oracle database dependencies
  - Add Oracle JDBC driver dependency compatible with Spring Boot 3.4+
  - Add HikariCP connection pool dependency (included by default in Spring Boot 3.4+)
  - Add Liquibase dependency for database schema management compatible with Spring Boot 3.4+
  - Ensure all dependencies support Java 17+ runtime
  - _Requirements: 2.2_

- [x] 3. Create basic Spring Boot application class
  - Create main application class with @SpringBootApplication for Spring Boot 3.4+
  - Add basic application.yml configuration file with Spring Boot 3.4+ property structure
  - Configure Java 17+ specific JVM options if needed
  - _Requirements: 2.1_

- [x] 4. Configure Oracle database connection properties
  - Add Oracle database connection URL, username, password to application.yml using Spring Boot 3.4+ configuration format
  - Configure JdbcTemplate and DataSource properties for Oracle with Spring Boot 3.4+ auto-configuration
  - Configure HikariCP connection pool settings optimized for Java 17+ and Spring Boot 3.4+
  - Configure Liquibase properties for schema management compatible with Spring Boot 3.4+
  - Configure SpringDoc OpenAPI v2 properties for Swagger UI with Spring Boot 3.4+ integration
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

- [x] 15. Create AuditEvent unit tests
  - Write tests for model creation and field validation using JUnit 5 and Spring Boot 3.4+ test features
  - Test Builder pattern functionality with Java 17+ language features
  - Test equals, hashCode, and toString methods ensuring compatibility with Java 17+ record patterns
  - Verify project builds and tests pass with Spring Boot 3.4+ and Java 17+ runtime
  - Use Test_ prefixed tables for any database testing scenarios (e.g., Test_PIPELINE_AUDIT_LOG)
  - _Requirements: 2.2, 8.2_

- [x] 16. Create AuditDetails model class
  - Create AuditDetails POJO for JSON metadata
  - Add fields for file metadata (size, hash)
  - Add fields for SQL loader statistics (rows read/loaded/rejected)
  - _Requirements: 2.4, 3.1, 3.2_

- [x] 17. Add more AuditDetails fields
  - Add fields for record counts and control totals
  - Add fields for business rule input/output data
  - Add proper getters and setters
  - _Requirements: 3.3, 3.4, 4.2, 4.3_

- [x] 18. Add JSON serialization to AuditDetails
  - Add Jackson annotations for JSON conversion compatible with Spring Boot 3.4+ Jackson version
  - Configure proper JSON include/exclude policies using Jackson 2.15+ features
  - Add builder pattern for easy construction with Java 17+ record support if applicable
  - _Requirements: 2.4_

- [x] 19. Create AuditDetails unit tests
  - Test JSON serialization and deserialization with Jackson 2.15+ and Spring Boot 3.4+
  - Test builder pattern functionality with Java 17+ language features
  - Test field validation and constraints ensuring Spring Boot 3.4+ compatibility
  - Use Test_ prefixed tables for any database testing scenarios
  - _Requirements: 2.4_

- [x] 20. Create basic AuditRepository class with JdbcTemplate
  - Create @Repository class with JdbcTemplate dependency injection using Spring Boot 3.4+ auto-configuration
  - Add basic save() method for inserting audit events with Java 17+ features
  - Add findById() method for retrieving single audit event using Optional return types
  - Use Test_ prefixed tables for any database testing scenarios (e.g., Test_PIPELINE_AUDIT_LOG)
  - Make sure all the concerned hooks are run
  - _Requirements: 2.1, 2.2_

- [x] 21. Add correlation ID query methods
  - Add findByCorrelationIdOrderByEventTimestamp method using SQL queries
  - Add countByCorrelationId method with JdbcTemplate
  - Implement proper SQL queries with parameter binding
  - Use Test_ prefixed tables for any database testing scenarios (e.g., Test_PIPELINE_AUDIT_LOG)
  - Make sure all the concerned hooks are run
  - _Requirements: 1.2, 6.1_

- [x] 22. Add source system and module query methods
  - Add findBySourceSystemAndCheckpointStage method with SQL
  - Add findByModuleNameAndStatus method with SQL
  - Implement proper WHERE clause filtering with parameters
  - Use Test_ prefixed tables for any database testing scenarios (e.g., Test_PIPELINE_AUDIT_LOG)
  - Make sure all the concerned hooks are run
  - _Requirements: 1.3, 6.2, 6.3_

- [x] 23. Add date range and statistics query methods
  - Add findByEventTimestampBetween method with SQL date filtering
  - Add countByCorrelationIdAndStatus method with SQL COUNT query
  - Add pagination support using LIMIT and OFFSET
  - Use Test_ prefixed tables for any database testing scenarios (e.g., Test_PIPELINE_AUDIT_LOG)
  - Make sure all the concerned hooks are run
  - _Requirements: 6.1, 6.4, 5.2_

- [x] 24. Create AuditRepository integration tests with JdbcTemplate
  - Set up @JdbcTest configuration with Liquibase for Spring Boot 3.4+ and Java 17+
  - Write tests for basic CRUD operations using TestJdbcTemplate with JUnit 5 and Spring Boot 3.4+ test features
  - Test correlation ID and source system queries with Java 17+ text blocks for SQL readability
  - Verify Liquibase schema creation works correctly with Spring Boot 3.4+ auto-configuration
  - Use Test_ prefixed tables (e.g., Test_PIPELINE_AUDIT_LOG) for all integration testing scenarios
  - Make sure all the concerned hooks are run
  - _Requirements: 2.1, 2.2, 6.1, 6.2_

- [x] 25. Test advanced repository query methods
  - Test date range queries and pagination
  - Test count queries and statistics methods
  - Test query performance with sample data
  - Make sure all the concerned hooks are run
  - _Requirements: 6.3, 6.4, 5.2_

- [x] 26. Create CorrelationIdManager interface
  - Define interface with generateCorrelationId method
  - Add getCurrentCorrelationId and setCorrelationId methods
  - Add clearCorrelationId method
  - Make sure all the concerned hooks are run
  - _Requirements: 1.2, 2.3_

- [x] 27. Implement CorrelationIdManager with ThreadLocal
  - Create implementation using ThreadLocal storage with Java 17+ enhanced features
  - Implement UUID generation with proper formatting using Java 17+ UUID improvements
  - Add thread safety and cleanup logic with virtual threads compatibility for future Java versions
  - Make sure all the concerned hooks are run
  - _Requirements: 1.2, 2.3_

- [x] 28. Create CorrelationIdManager unit tests
  - Test UUID generation and uniqueness with Java 17+ UUID improvements
  - Test thread-local storage and isolation ensuring Spring Boot 3.4+ compatibility
  - Test cleanup and memory leak prevention with virtual threads compatibility
  - Use Test_ prefixed tables for any database testing scenarios
  - Make sure all the concerned hooks are run
  - _Requirements: 1.2, 1.4_

- [x] 29. Create basic AuditService interface
  - Define AuditService interface with core audit logging methods
  - Add method signatures for all checkpoint logging operations
  - Include proper JavaDoc documentation
  - Make sure all the concerned hooks are run
  - _Requirements: 2.1, 7.1, 7.2, 7.3, 7.4_

- [x] 30. Implement basic AuditService with Oracle persistence
  - Create AuditServiceImpl with Oracle database integration using Spring Boot 3.4+ features
  - Implement core audit event creation and persistence logic with Java 17+ pattern matching and records where applicable
  - Add basic validation and error handling using Spring Boot 3.4+ validation framework
  - Make sure all the concerned hooks are run
  - _Requirements: 2.1, 2.2, 2.5, 8.1_

- [x] 31. Add checkpoint-specific logging methods
  - Implement logFileTransfer method for Checkpoint 1 using Java 17+ enhanced switch expressions
  - Implement logSqlLoaderOperation method for Checkpoint 2 with improved error handling
  - Add proper Oracle transaction management using Spring Boot 3.4+ transaction features
  - Make sure all the concerned hooks are run
  - _Requirements: 7.1, 7.2, 4.1, 4.2_

- [x] 32. Complete remaining checkpoint methods
  - Implement logBusinessRuleApplication method for Checkpoint 3
  - Implement logFileGeneration method for Checkpoint 4
  - Add comprehensive error handling for Oracle operations
  - Make sure all the concerned hooks are run
  - _Requirements: 7.3, 7.4, 4.3, 4.4_

- [x] 33. Create AuditService unit tests
  - Write unit tests with mocked Oracle repository using Spring Boot 3.4+ test features
  - Test all checkpoint logging methods with Java 17+ language enhancements
  - Test error handling and validation scenarios ensuring Spring Boot 3.4+ compatibility
  - Use Test_ prefixed tables for any database testing scenarios
  - Make sure all the concerned hooks are run
  - _Requirements: 2.1, 2.2, 7.5_

- [x] 34. Create Swagger configuration
  - Create SwaggerConfig class with @Configuration for SpringDoc OpenAPI v2 and Spring Boot 3.4+
  - Configure OpenAPI 3.0 documentation with title, description, version using SpringDoc v2 annotations
  - Set up API servers for development and production with Spring Boot 3.4+ server configuration
  - Add contact information and API grouping using SpringDoc v2 features
  - Make sure all the concerned hooks are run
  - _Requirements: 6.7_

- [x] 35. Create REST API controller structure with Swagger annotations
  - Create AuditDashboardController class with @RestController for Spring Boot 3.4+
  - Add @RequestMapping for /api/audit base path using Spring Boot 3.4+ path matching
  - Add @Tag annotation for SpringDoc OpenAPI v2 API grouping
  - Set up basic Spring MVC configuration compatible with Spring Boot 3.4+ and Java 17+
  - Make sure all the concerned hooks are run
  - _Requirements: 6.1, 6.6, 6.7_

- [x] 36. Implement audit events REST endpoint with Swagger documentation
  - Create GET /api/audit/events endpoint with pagination using Spring Boot 3.4+ data features
  - Add @Operation annotation with summary and description for SpringDoc OpenAPI v2
  - Add @Parameter annotations for query parameters with OpenAPI 3.0 schema definitions
  - Add @ApiResponse annotations for different response codes with proper media types
  - Return JSON responses with proper HTTP status codes using Spring Boot 3.4+ ResponseEntity enhancements
  - Make sure all the concerned hooks are run
  - _Requirements: 6.1, 6.2, 6.6, 6.7_

- [x] 37. Add reconciliation REST endpoints with Swagger documentation
  - Create GET /api/audit/reconciliation/{correlationId} endpoint with Spring Boot 3.4+ path variable handling
  - Implement GET /api/audit/reconciliation/reports with filtering using Spring Boot 3.4+ request parameter binding
  - Add @Operation and @Parameter annotations for SpringDoc OpenAPI v2 documentation
  - Return reconciliation data in JSON format with Jackson 2.15+ serialization features
  - Make sure all the concerned hooks are run
  - _Requirements: 5.1, 5.2, 5.6, 6.7_

- [x] 38. Create statistics and discrepancy REST endpoints with Swagger
  - Implement GET /api/audit/statistics endpoint with Spring Boot 3.4+ metrics integration
  - Create GET /api/audit/discrepancies endpoint with filters using Spring Boot 3.4+ filtering capabilities
  - Add comprehensive SpringDoc OpenAPI v2 annotations for all parameters with OpenAPI 3.0 schemas
  - Add proper error handling and HTTP status codes using Spring Boot 3.4+ exception handling features
  - _Requirements: 6.4, 5.4, 6.6, 6.7_

- [x] 39. Add REST API request/response DTOs with Swagger schemas
  - Create AuditEventDTO for API responses using Java 17+ records where appropriate
  - Create ReconciliationReportDTO for report endpoints with Java 17+ sealed classes if applicable
  - Add @Schema annotations for SpringDoc OpenAPI v2 documentation with OpenAPI 3.0 schema definitions
  - Add proper JSON serialization annotations compatible with Jackson 2.15+ and Spring Boot 3.4+
  - Make sure all the concerned hooks are run
  - _Requirements: 6.1, 5.6, 6.6, 6.7_

- [x] 40. Create REST API integration tests including Swagger
  - Write @WebMvcTest tests for all endpoints using Spring Boot 3.4+ test framework and JUnit 5
  - Test JSON request/response serialization with Jackson 2.15+ and Java 17+ features
  - Test pagination, filtering, and error scenarios with Spring Boot 3.4+ test utilities
  - Verify SpringDoc OpenAPI v2 Swagger UI accessibility and documentation generation
  - Use Test_ prefixed tables for any database testing scenarios
  - Make sure all the concerned hooks are run
  - _Requirements: 6.1, 6.2, 6.4, 6.6, 6.7_

- [x] 41. Add Oracle-specific configuration
  - Configure Oracle DataSource and connection pool settings optimized for Spring Boot 3.4+ and Java 17+
  - Add Oracle-specific JdbcTemplate properties for performance with Spring Boot 3.4+ auto-configuration
  - Configure transaction management for Oracle with JdbcTemplate using Spring Boot 3.4+ transaction features
  - Configure Liquibase for Oracle schema management compatible with Spring Boot 3.4+ and Java 17+
  - Make sure all the concerned hooks are run
  - _Requirements: 2.2, 2.5_

- [x] 42. Create database integration tests with Oracle and Liquibase
  - Set up @JdbcTest with Oracle test configuration for Spring Boot 3.4+ and Java 17+
  - Test all repository methods with Oracle database using JdbcTemplate and Spring Boot 3.4+ test features
  - Verify Liquibase schema creation and migrations with Spring Boot 3.4+ Liquibase integration
  - Test Oracle-specific features and SQL queries using Java 17+ text blocks for better SQL readability
  - Use Test_ prefixed tables (e.g., Test_PIPELINE_AUDIT_LOG) for all database integration testing
  - Make sure all the concerned hooks are run
  - _Requirements: 2.2, 2.5, 6.1, 6.2_

- [x] 43. Implement comprehensive error handling
  - Create audit-specific exception classes using Java 17+ sealed classes where appropriate
  - Add global exception handler for REST API with SpringDoc OpenAPI v2 error documentation
  - Implement retry logic for Oracle connection issues with JdbcTemplate using Spring Boot 3.4+ retry mechanisms
  - Make sure all the concerned hooks are run
  - _Requirements: 7.5, 8.4, 6.6, 6.7_

- [x] 44. Add API security configuration
  - Configure Spring Security 6.x for REST endpoints compatible with Spring Boot 3.4+
  - Add authentication and authorization for audit APIs using Spring Security 6.x features
  - Implement role-based access control with Spring Boot 3.4+ security auto-configuration
  - Configure SpringDoc OpenAPI v2 Swagger UI security for protected endpoints with OAuth2/JWT integration
  - Make sure all the concerned hooks are run
  - _Requirements: 8.3, 6.1, 6.7_

- [x] 45. Create end-to-end integration tests
  - Test complete audit flow from service to Oracle database using JdbcTemplate with Spring Boot 3.4+ and Java 17+
  - Test REST API endpoints with real Oracle database using Spring Boot 3.4+ test framework
  - Verify correlation ID propagation across all components with Java 17+ enhanced debugging features
  - Test Liquibase migrations in integration environment with Spring Boot 3.4+ Liquibase integration
  - Verify SpringDoc OpenAPI v2 Swagger UI functionality and API documentation generation
  - Use Test_ prefixed tables for all end-to-end integration testing scenarios
  - Make sure all the concerned hooks are run
  - _Requirements: 1.1, 1.2, 2.5, 6.6, 6.7_