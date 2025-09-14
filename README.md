# Batch Audit System

A comprehensive end-to-end audit trail system for enterprise data processing pipelines built with Spring Boot 3.4+ and Oracle Database.

## Overview

The Batch Audit System provides complete traceability for data processing pipelines that ingest files from multiple mainframe source systems, process them through Oracle staging databases, apply business logic via Java modules, and generate final output files. The system uses Spring JdbcTemplate for direct SQL operations with Oracle database for optimal performance and control.

**🎉 PRODUCTION READY**: Complete implementation with REST API, security, comprehensive testing, and Oracle integration.

### Key Features

- **Multi-source system support** with distinct audit trails and correlation ID tracking
- **Checkpoint-based logging** at critical pipeline transition points with @Auditable annotation
- **REST API dashboard** for real-time monitoring, historical analysis, and reconciliation reports
- **Automated reconciliation reports** with discrepancy detection and data integrity verification
- **Oracle database persistence** with optimized indexing and HikariCP connection pooling
- **Spring Security 6.x** with JWT authentication and role-based access control
- **Comprehensive error handling** with retry mechanisms and circuit breaker patterns
- **SpringDoc OpenAPI v2** with interactive Swagger UI documentation
- **Liquibase schema management** for database version control and migrations

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
- **Java 17+**: Target runtime environment with enhanced language features
- **Jackson**: JSON serialization for audit details and API responses
- **SpringDoc OpenAPI v2**: Interactive API documentation with Swagger UI
- **JUnit 5**: Unit and integration testing framework
- **Mockito**: Mocking framework for service layer testing
- **Spring Retry**: Resilience patterns for Oracle database operations

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

**🎉 PRODUCTION READY**: All 45 tasks completed (100% progress). The system is ready for production deployment with comprehensive REST API, Oracle integration, security framework, and extensive testing coverage.

### Current Status: All Phases Complete ✅
- ✅ **Phase 1 (Tasks 1-16)**: Foundation - Maven setup, Oracle configuration, core entities
- ✅ **Phase 2 (Tasks 17-25)**: Data Layer - Models, repositories, database integration  
- ✅ **Phase 3 (Tasks 26-33)**: Service Layer - Business logic and audit services
- ✅ **Phase 4 (Tasks 34-45)**: REST API & Integration - Complete API, security, comprehensive testing

### What's Implemented

#### Core Foundation ✅
- Maven project structure with Spring Boot 3.4.0 and Java 17+
- Oracle JDBC driver (ojdbc11) and HikariCP connection pooling
- SpringDoc OpenAPI v2 for comprehensive Swagger UI documentation
- Complete Liquibase setup with schema migrations and Oracle optimization
- PIPELINE_AUDIT_LOG table with proper Oracle data types and strategic indexing

#### Data Models & Repository ✅
- **AuditEvent**: Complete entity with Builder pattern and comprehensive validation
- **AuditDetails**: Full JSON metadata model with Jackson serialization
- **Enumerations**: AuditStatus and CheckpointStage with full test coverage
- **AuditRepository**: JdbcTemplate-based data access with Oracle-optimized queries
- **Query Methods**: Correlation ID, source system, date range, and pagination support

#### Service Layer ✅
- **CorrelationIdManager**: Thread-safe correlation ID management with virtual thread compatibility
- **AuditService**: Complete business logic with checkpoint-specific logging methods
- **Transaction Management**: Oracle-specific transaction handling with retry mechanisms
- **Error Handling**: Comprehensive exception hierarchy with graceful degradation

#### REST API Layer ✅
- **Complete REST API**: All dashboard and reporting endpoints implemented
- **AuditDashboardController**: Comprehensive controller with pagination and filtering
- **API DTOs**: Java 17+ records and sealed classes for type-safe API responses
- **SpringDoc OpenAPI v2**: Complete Swagger UI with interactive API testing
- **Security Integration**: Spring Security 6.x with JWT authentication and RBAC
- **Error Handling**: Global exception handler with audit-specific error responses

#### Testing & Quality ✅
- **Comprehensive Test Coverage**: Unit, integration, and end-to-end tests
- **Oracle Integration Tests**: Complete database testing with Test_ prefixed tables
- **API Testing**: @WebMvcTest with Spring Boot 3.4+ test framework
- **Security Testing**: Authentication and authorization validation
- **Performance Testing**: Query optimization and connection pooling validation

### Key Features Delivered
- **Multi-source system support** with distinct audit trails and correlation ID tracking
- **Checkpoint-based logging** at critical pipeline transition points using @Auditable annotation
- **REST API dashboard** for real-time monitoring, historical analysis, and reconciliation reports
- **Automated reconciliation reports** with discrepancy detection and data integrity verification
- **Oracle database persistence** with optimized indexing and HikariCP connection pooling
- **Spring Security 6.x integration** with JWT authentication and role-based access control
- **SpringDoc OpenAPI v2** with interactive Swagger UI for API documentation and testing
- **Comprehensive error handling** with retry mechanisms, circuit breaker patterns, and graceful degradation
- **Production-ready configuration** with environment-specific profiles and Oracle optimization

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
   
   # View Swagger UI with complete API documentation
   open http://localhost:8080/audit/swagger-ui.html
   
   # Test API endpoints (requires authentication)
   curl -H "Authorization: Bearer <jwt-token>" \
     http://localhost:8080/audit/api/audit/events?page=0&size=10
   ```

7. **Access API Documentation**
   - **Swagger UI**: http://localhost:8080/audit/swagger-ui.html
   - **OpenAPI Specification**: http://localhost:8080/audit/api-docs
   - **Health Monitoring**: http://localhost:8080/audit/actuator/health
   - **Metrics**: http://localhost:8080/audit/actuator/metrics

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

The system provides a comprehensive REST API for dashboard and reporting functionality. All endpoints require JWT authentication with appropriate roles.

### Audit Events
- `GET /api/audit/events` - Retrieve paginated audit events with filtering by source system, module, status, and checkpoint stage
- **Parameters**: sourceSystem, moduleName, status, checkpointStage, page, size
- **Response**: Paginated list of AuditEventDTO with metadata

### Reconciliation Reports  
- `GET /api/audit/reconciliation/{correlationId}` - Generate comprehensive reconciliation report for a pipeline run
- `GET /api/audit/reconciliation/{correlationId}/dto` - Get reconciliation report with specified detail level (STANDARD, DETAILED, SUMMARY)
- `GET /api/audit/reconciliation/reports` - List all reconciliation reports with filtering by source system, status, and date range
- **Features**: Data integrity verification, record count analysis, discrepancy detection

### Statistics and Analytics
- `GET /api/audit/statistics` - Generate comprehensive audit statistics for specified date ranges
- **Parameters**: startDate (required), endDate (required)
- **Response**: Event counts by status/source/module, success rates, trend analysis

### Data Discrepancies
- `GET /api/audit/discrepancies` - Identify and retrieve data discrepancies with filtering
- **Parameters**: sourceSystem, moduleName, severity, status, startDate, endDate
- **Features**: Record count mismatches, control total discrepancies, processing timeouts

### API Documentation
- **Swagger UI**: Available at `/audit/swagger-ui.html` with interactive API testing
- **OpenAPI Spec**: Available at `/audit/api-docs` for client library generation
- **Authentication**: JWT token support with role-based access control (AUDIT_USER, AUDIT_ADMIN)
- **Security Integration**: OAuth2/JWT configuration for Swagger UI testing

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
  retry:
    enabled: true
    default:
      max-attempts: 3
      initial-delay: 1000
      max-delay: 30000
      multiplier: 2.0
  security:
    jwt:
      jwk-set-uri: ${JWT_JWK_SET_URI:}
      issuer: ${JWT_ISSUER:}

spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    contexts: ${spring.profiles.active}
    drop-first: false
    enabled: true
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${JWT_JWK_SET_URI:}
          issuer: ${JWT_ISSUER:}

springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
```

## Architecture Patterns

- **Layered Architecture**: Clear separation between presentation, service, repository, and entity layers with strict dependency flow
- **Repository Pattern**: Data access abstraction using Spring JdbcTemplate with Oracle optimization and HikariCP pooling
- **Checkpoint-Based Logging**: Structured audit capture at critical pipeline transition points using @Auditable annotation
- **Correlation ID Management**: Thread-safe tracking of related audit events across pipeline stages with virtual thread compatibility
- **REST API Design**: Comprehensive dashboard and reporting endpoints with pagination, filtering, and OpenAPI documentation
- **Aspect-Oriented Programming (AOP)**: Automatic audit capture with @Auditable annotation processing and retry mechanisms
- **Event-Driven Architecture**: Spring Application Events for decoupled audit logging and system integration
- **Circuit Breaker Pattern**: Resilience for audit system failures without breaking main business flow
- **Security-First Design**: JWT authentication, role-based access control, and OAuth2 resource server integration

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