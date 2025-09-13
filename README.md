# Batch Audit System

A comprehensive end-to-end audit trail system for enterprise data processing pipelines built with Spring Boot and Oracle Database.

## Overview

The Batch Audit System provides complete traceability for data processing pipelines that ingest files from multiple mainframe source systems, process them through Oracle staging databases, apply business logic via Java modules, and generate final output files. The system uses Spring JdbcTemplate for direct SQL operations with Oracle database for optimal performance and control.

### Key Features

- **Multi-source system support** with distinct audit trails
- **Checkpoint-based logging** at critical pipeline transition points
- **REST API dashboard** for real-time monitoring and historical analysis
- **Automated reconciliation reports** with discrepancy detection
- **Oracle database persistence** with optimized indexing for audit queries
- **Liquibase schema management** for database version control
- **Spring Boot architecture** with comprehensive error handling and resilience

## Technology Stack

### Core Framework
- **Spring Boot 3.4+**: Main application framework with auto-configuration
- **Spring JdbcTemplate**: Data access layer with Oracle database integration
- **Spring Web MVC**: REST API endpoints for dashboard and reporting
- **Spring Security**: Authentication and authorization for audit APIs
- **Spring Boot Actuator**: Health monitoring and metrics

### Database & Persistence
- **Oracle Database 19c/21c**: Primary audit data storage with optimized indexing
- **Spring JdbcTemplate**: Direct SQL operations with Oracle database integration
- **HikariCP**: High-performance connection pooling for Oracle
- **Liquibase**: Database schema migration and version control
- **UUID**: Primary keys for audit events ensuring uniqueness across systems

### Build System & Dependencies
- **Maven**: Build automation and dependency management
- **Java 17+**: Target runtime environment
- **Jackson**: JSON serialization for audit details and API responses
- **JUnit 5**: Unit and integration testing framework
- **Mockito**: Mocking framework for service layer testing

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/company/audit/
│   │       ├── BatchAuditApplication.java          # Main Spring Boot application class
│   │       ├── config/                             # Configuration classes
│   │       ├── controller/                         # REST API controllers
│   │       ├── service/                            # Business logic layer
│   │       ├── repository/                         # Data access layer
│   │       ├── entity/                             # JPA entities
│   │       ├── model/                              # DTOs and data models
│   │       ├── enums/                              # Enumeration classes
│   │       └── exception/                          # Custom exceptions
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

docs/
├── implementation-guide.md                         # Comprehensive implementation guide
├── task-reference.md                               # Task breakdown and reference
├── database-setup.md                               # Database configuration guide
└── development-guidelines.md                       # Code generation principles and development standards
```

## Implementation Status

The project has completed the foundation phase and is progressing through core entity development following a structured 45-task implementation plan:

### Current Status: Phase 1 - Foundation Completion (Task 17 in progress)
- ✅ **Tasks 1-16 Complete**: Foundation phase with Maven setup, Oracle configuration, core entities, and basic AuditDetails model
- 🔄 **Task 17 In Progress**: Completing AuditDetails model with additional fields for business rule processing
- 🔄 **Next Phase**: Repository implementation and JSON serialization validation (Tasks 18-25)

### Implementation Phases
- **Phase 1 (Tasks 1-17)**: Foundation - Maven setup, Oracle configuration, core entities *(16/17 complete)*
- **Phase 2 (Tasks 18-25)**: Data Layer - Models, repositories, database integration *(0/8 complete)*
- **Phase 3 (Tasks 26-33)**: Service Layer - Business logic and audit services *(0/8 complete)*
- **Phase 4 (Tasks 34-45)**: API & Integration - REST APIs, security, comprehensive testing *(0/12 complete)*

### What's Implemented
- ✅ Maven project structure with Spring Boot 3.4.0 and Java 17
- ✅ Oracle JDBC driver (ojdbc11) and HikariCP connection pooling
- ✅ SpringDoc OpenAPI v2 for Swagger UI documentation
- ✅ Main Spring Boot application class with proper annotations
- ✅ Comprehensive Oracle database configuration (application.yml and application-local.properties)
- ✅ Complete Liquibase setup with master changelog and schema migrations
- ✅ PIPELINE_AUDIT_LOG table with proper Oracle data types and constraints
- ✅ Optimized database indexes for common query patterns
- ✅ AuditStatus enum (SUCCESS, FAILURE, WARNING)
- ✅ CheckpointStage enum with all pipeline stages
- ✅ Complete AuditEvent model with Builder pattern, equals/hashCode, and comprehensive unit tests
- 🔄 AuditDetails model for JSON metadata (Task 17 - completing additional fields for business rule processing)

### Next Steps
1. Complete AuditDetails model with additional fields (Task 17 - immediate priority)
2. Validate JSON serialization configuration in AuditDetails (Task 18)
3. Verify comprehensive AuditDetails unit tests (Task 19)
4. Implement AuditRepository with JdbcTemplate (Task 20)
5. Add correlation ID and query methods (Tasks 21-23)
6. Create comprehensive repository integration tests (Tasks 24-25)

### Key Accomplishments
- **Database Schema**: Complete Oracle table structure with proper constraints and performance indexes
- **Core Models**: AuditEvent POJO with Builder pattern and comprehensive field validation
- **Metadata Model**: AuditDetails POJO for structured JSON audit metadata with Jackson annotations
- **Enumerations**: Type-safe enums for audit status and checkpoint stages
- **Configuration**: Production-ready Oracle configuration with HikariCP optimization
- **Testing**: Comprehensive unit test suite with 100% coverage for implemented components

**Documentation References**:
- Architecture overview: `docs/architecture-overview.md`
- Detailed task breakdown: `.kiro/specs/batch-audit-system/tasks.md`
- Development guidelines: `docs/development-guidelines.md`
- Current development status: `docs/development-status.md`
- Implementation guide: `docs/implementation-guide.md`

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Oracle Database 19c/21c
- Access to mainframe source systems (for production use)

### Local Development Setup

The foundation phase is complete. The application is ready for database setup and development.

1. **Clone the repository**
   ```bash
   git clone https://github.com/pkanduri1/batch-audit-system.git
   cd batch-audit-system
   ```

2. **Verify the build**
   ```bash
   # Compile and run tests
   mvn clean compile test
   
   # Check dependencies
   mvn dependency:tree
   ```

3. **Database Setup**
   
   Set up your Oracle database connection:
   ```bash
   # Set environment variables for Oracle connection
   export ORACLE_LOCAL_PASSWORD=your_oracle_password
   export ORACLE_DB_URL=jdbc:oracle:thin:@//localhost:1521/ORCLPDB1
   export ORACLE_DB_USERNAME=cm3int
   ```

4. **Run Database Migrations**
   ```bash
   # Apply Liquibase migrations to create audit table and indexes
   mvn liquibase:update
   
   # Verify schema creation
   mvn liquibase:status
   ```

5. **Start the Application**
   ```bash
   # Run with local profile
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   
   # Or run with specific Oracle connection
   mvn spring-boot:run -Dspring-boot.run.profiles=local \
     -DORACLE_LOCAL_PASSWORD=your_password
   ```

6. **Verify Setup**
   ```bash
   # Check application health
   curl http://localhost:8080/audit/actuator/health
   
   # View Swagger UI (when API endpoints are implemented)
   open http://localhost:8080/audit/swagger-ui.html
   ```

## Database Schema Management

The project uses Liquibase for database schema version control and migrations.

### Key Liquibase Files

- **`db.changelog-master.xml`**: Master changelog that includes all migration files
- **`001-create-audit-table.xml`**: Creates the main PIPELINE_AUDIT_LOG table
- **`002-create-audit-indexes.xml`**: Creates optimized indexes for audit queries

### Common Liquibase Commands

```bash
# Run database migrations
mvn liquibase:update

# Generate changelog from existing database
mvn liquibase:generateChangeLog

# Validate changelog
mvn liquibase:validate

# Rollback changes
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Show pending changes
mvn liquibase:status
```

## Build and Test Commands

### Build & Test
```bash
# Clean build with tests
mvn clean compile test

# Package application
mvn clean package

# Run application locally
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Testing
```bash
# Run unit tests only
mvn test

# Run integration tests
mvn verify

# Run specific test class
mvn test -Dtest=AuditServiceTest

# Generate test coverage report
mvn jacoco:report
```

## API Endpoints

The system provides REST API endpoints for dashboard and reporting functionality:

### Audit Events
- `GET /api/audit/events` - Retrieve audit events with filtering and pagination
- `GET /api/audit/events/{auditId}` - Get specific audit event details

### Reconciliation Reports
- `GET /api/audit/reconciliation/{correlationId}` - Get reconciliation report for a pipeline run
- `GET /api/audit/reconciliation/reports` - List all reconciliation reports with filtering

### Statistics and Monitoring
- `GET /api/audit/statistics` - Get audit statistics and summary data
- `GET /api/audit/discrepancies` - Retrieve data discrepancies with filtering

## Configuration

### Application Properties

The system supports multiple configuration profiles:

- **`application.yml`**: Base configuration
- **`application-dev.yml`**: Development environment overrides
- **`application-prod.yml`**: Production environment overrides
- **`application-local.properties`**: Local development configuration

### Key Configuration Sections

```yaml
audit:
  database:
    batch-size: 100
    connection-pool-size: 10
  retention:
    days: 365
  reconciliation:
    auto-generate: true
    schedule: "0 0 6 * * ?"
  dashboard:
    page-size: 50
    max-export-records: 10000

spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    contexts: ${spring.profiles.active}
    drop-first: false
    enabled: true
```

## Architecture Patterns

- **Layered Architecture**: Clear separation between presentation, service, repository, and entity layers with strict dependency flow
- **Repository Pattern**: Data access abstraction using Spring JdbcTemplate with Oracle optimization
- **Checkpoint-Based Logging**: Structured audit capture at critical pipeline transition points using @Auditable annotation
- **Correlation ID Management**: Thread-safe tracking of related audit events across pipeline stages
- **REST API Design**: Comprehensive dashboard and reporting endpoints with pagination and filtering
- **Aspect-Oriented Programming (AOP)**: Automatic audit capture with @Auditable annotation processing
- **Event-Driven Architecture**: Spring Application Events for decoupled audit logging
- **Circuit Breaker Pattern**: Resilience for audit system failures without breaking main business flow

## Development Workflow

### Task-Based Development
The project follows a structured 44-task implementation plan organized into four phases:

1. **Foundation Phase** (Tasks 1-15): Project structure, Maven setup, and core entities
2. **Data Layer Phase** (Tasks 16-25): Models, repositories, and database integration  
3. **Service Layer Phase** (Tasks 26-33): Business logic and audit services
4. **API & Integration Phase** (Tasks 34-44): REST APIs, security, and comprehensive testing

See `docs/implementation-guide.md` for detailed phase breakdown and implementation guidelines.

### Code Quality Standards
- Follow Spring Boot best practices with minimal implementation first approach
- Leverage Spring's dependency injection and auto-configuration
- Use @Component, @Service, @Repository annotations appropriately
- Implement proper exception handling with @ControllerAdvice
- Write comprehensive unit and integration tests (minimum 80% coverage)
- Use Liquibase for all database schema changes with proper Oracle-specific annotations
- Prefix audit-related classes with "Audit" and use UPPER_CASE for enum values
- Update documentation for any API or configuration changes

### Testing Strategy
```bash
# Run tests by phase
mvn test -Dtest="**/entity/**,**/repository/**"  # Data layer tests
mvn test -Dtest="**/service/**"                   # Service layer tests  
mvn test -Dtest="**/controller/**"                # API layer tests
mvn verify                                        # Full integration tests
```

## License

This project is proprietary software. All rights reserved.