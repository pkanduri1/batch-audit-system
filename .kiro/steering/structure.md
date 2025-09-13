# Project Structure

## Maven Project Layout

```
src/
├── main/
│   ├── java/
│   │   └── com/company/audit/
│   │       ├── BatchAuditApplication.java          # Main Spring Boot application class
│   │       ├── config/                             # Configuration classes
│   │       │   ├── AuditDatabaseConfig.java        # Oracle database configuration
│   │       │   ├── AuditSecurityConfig.java        # Security and API access control
│   │       │   └── AuditAopConfig.java             # AOP configuration for @Auditable
│   │       ├── controller/                         # REST API controllers
│   │       │   └── AuditDashboardController.java   # Dashboard and reporting endpoints
│   │       ├── service/                            # Business logic layer
│   │       │   ├── AuditService.java               # Core audit operations interface
│   │       │   ├── AuditServiceImpl.java           # Audit service implementation
│   │       │   ├── ReconciliationService.java      # Report generation and data integrity
│   │       │   └── CorrelationIdManager.java       # Correlation ID management
│   │       ├── repository/                         # Data access layer
│   │       │   └── AuditRepository.java            # JPA repository for audit events
│   │       ├── entity/                             # JPA entities
│   │       │   └── AuditEvent.java                 # Main audit event entity
│   │       ├── model/                              # DTOs and data models
│   │       │   ├── AuditDetails.java               # JSON metadata model
│   │       │   ├── ReconciliationReport.java       # Report data structure
│   │       │   └── dto/                            # API request/response DTOs
│   │       ├── enums/                              # Enumeration classes
│   │       │   ├── AuditStatus.java                # SUCCESS, FAILURE, WARNING
│   │       │   └── CheckpointStage.java            # Pipeline checkpoint identifiers
│   │       ├── aspect/                             # AOP aspects
│   │       │   └── AuditAspect.java                # @Auditable annotation processing
│   │       ├── exception/                          # Custom exceptions
│   │       │   ├── AuditException.java             # Base audit exception
│   │       │   └── AuditPersistenceException.java  # Database-related failures
│   │       └── event/                              # Spring application events
│   │           ├── FileTransferEvent.java          # File transfer audit events
│   │           └── BusinessRuleEvent.java          # Business logic audit events
│   └── resources/
│       ├── application.yml                         # Main configuration
│       ├── application-dev.yml                     # Development profile
│       ├── application-prod.yml                    # Production profile
│       └── db/changelog/                           # Liquibase database migration scripts
│           ├── db.changelog-master.xml             # Master changelog file
│           ├── 001-create-audit-table.xml          # Initial audit table creation
│           └── 002-create-audit-indexes.xml        # Audit table indexes
└── test/
    ├── java/
    │   └── com/company/audit/
    │       ├── service/                            # Service layer unit tests
    │       ├── repository/                         # Repository integration tests
    │       ├── controller/                         # REST API tests
    │       └── integration/                        # End-to-end integration tests
    └── resources/
        ├── application-test.yml                    # Test configuration
        └── test-data/                              # Test data files
```

## Key Organizational Principles

### Package Structure
- **Layered by Function**: Clear separation between controllers, services, repositories, and entities
- **Single Responsibility**: Each package contains classes with related responsibilities
- **Dependency Direction**: Dependencies flow downward (controller → service → repository → entity)

### Naming Conventions
- **Entities**: Singular nouns (AuditEvent, not AuditEvents)
- **Services**: Business domain names with Service suffix (AuditService, ReconciliationService)
- **Controllers**: Resource-based names with Controller suffix (AuditDashboardController)
- **Repositories**: Entity name + Repository suffix (AuditRepository)
- **DTOs**: Purpose-based names with DTO suffix (AuditEventDTO, ReconciliationReportDTO)

### Configuration Files
- **application.yml**: Environment-agnostic base configuration
- **Profile-specific configs**: Override base settings for different environments
- **External configuration**: Database credentials and sensitive data via environment variables
- **Feature toggles**: Audit system features controlled via configuration properties

### Testing Structure
- **Unit Tests**: Mirror main package structure in test directory
- **Integration Tests**: Separate integration package for cross-layer testing
- **Test Configuration**: Dedicated test profiles and configuration files
- **Test Data**: Centralized test data management in resources/test-data

### Database Schema
- **Table Naming**: UPPER_CASE with underscores (PIPELINE_AUDIT_LOG)
- **Column Naming**: camelCase in Java entities, snake_case in database
- **Indexes**: Optimized for common query patterns (correlationId, sourceSystem, eventTimestamp)
- **Constraints**: Enforce data integrity at database level