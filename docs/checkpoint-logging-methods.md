# Checkpoint-Specific Logging Methods

## Overview

The Batch Audit System implements four checkpoint-specific logging methods that capture audit events at critical stages of the data processing pipeline. Each method is designed to handle specific types of operations with tailored validation, error handling, and metadata capture.

## Implementation Status: ✅ Complete (Task 31)

All four checkpoint-specific logging methods have been fully implemented in `AuditServiceImpl` with comprehensive error handling, transaction management, and Java 17+ language features.

## Checkpoint Methods

### 1. File Transfer Logging (Checkpoint 1)

```java
public void logFileTransfer(UUID correlationId, String sourceSystem, String fileName, 
                           String processName, String sourceEntity, String destinationEntity, 
                           String keyIdentifier, AuditStatus status, String message, 
                           AuditDetails auditDetails)
```

**Purpose**: Logs file transfer events when files arrive from mainframe systems to RHEL.

**Key Features**:
- Automatic checkpoint stage assignment (`RHEL_LANDING`)
- Enhanced message formatting using Java 17+ text blocks
- File metadata validation (size, hash, integrity)
- Status-specific default message generation

**Audit Details Captured**:
- File size in bytes
- SHA-256 hash for integrity verification
- Transfer statistics and metadata

### 2. SQL*Loader Operation Logging (Checkpoint 2)

```java
public void logSqlLoaderOperation(UUID correlationId, String sourceSystem, String tableName, 
                                 String processName, String sourceEntity, String destinationEntity, 
                                 String keyIdentifier, AuditStatus status, String message, 
                                 AuditDetails auditDetails)
```

**Purpose**: Logs SQL*Loader operations when data is ingested from files to Oracle staging tables.

**Key Features**:
- Dynamic checkpoint stage determination (`SQLLOADER_START` or `SQLLOADER_COMPLETE`)
- Oracle table name validation
- Load statistics validation (negative values prevented)
- Enhanced message building with load statistics

**Audit Details Captured**:
- Rows read, loaded, and rejected counts
- Control totals for reconciliation
- Load performance metrics

### 3. Business Rule Application Logging (Checkpoint 3)

```java
public void logBusinessRuleApplication(UUID correlationId, String sourceSystem, String moduleName, 
                                      String processName, String sourceEntity, String destinationEntity, 
                                      String keyIdentifier, AuditStatus status, String message, 
                                      AuditDetails auditDetails)
```

**Purpose**: Logs business rule application events when Java modules apply transformations.

**Key Features**:
- Fixed checkpoint stage (`LOGIC_APPLIED`)
- Business rule validation and metadata capture
- Transformation details logging
- Rule input/output data preservation

**Audit Details Captured**:
- Rule input and output data as JSON maps
- Applied rule name and entity identifier
- Record count transformations (before/after)
- Transformation details and processing metadata

### 4. File Generation Logging (Checkpoint 4)

```java
public void logFileGeneration(UUID correlationId, String sourceSystem, String fileName, 
                             String processName, String sourceEntity, String destinationEntity, 
                             String keyIdentifier, AuditStatus status, String message, 
                             AuditDetails auditDetails)
```

**Purpose**: Logs file generation events when final output files are created.

**Key Features**:
- Fixed checkpoint stage (`FILE_GENERATED`)
- Fixed module name (`FILE_GENERATOR`)
- Output file metadata validation
- Generation statistics and control totals

**Audit Details Captured**:
- Output file size and hash
- Record counts in generated files
- Control totals for end-to-end reconciliation
- Generation performance metrics

## Common Features Across All Methods

### Transaction Management
- **Isolation Level**: `READ_COMMITTED` for consistent data access
- **Propagation**: `REQUIRES_NEW` for independent audit transactions
- **Timeout**: 30 seconds for database operations
- **Rollback**: Automatic rollback on `AuditPersistenceException` and `RuntimeException`

### Error Handling
- **Parameter Validation**: Comprehensive null and empty string checks
- **Business Logic Validation**: Domain-specific validation rules
- **Exception Hierarchy**: Specific exceptions for different failure types
- **Graceful Degradation**: Audit failures don't break main business flow

### Enhanced Features (Java 17+)
- **Text Blocks**: Multi-line SQL queries and message formatting
- **Switch Expressions**: Enhanced status-based message generation
- **Pattern Matching**: Type-safe parameter validation
- **Records**: Structured data handling where applicable

### Logging and Monitoring
- **Structured Logging**: Correlation ID and context in all log messages
- **Performance Metrics**: Additional metrics logging for monitoring
- **Debug Information**: Detailed debug logs for troubleshooting
- **Error Context**: Rich error information for support teams

## Usage Examples

### File Transfer Example
```java
auditService.logFileTransfer(
    correlationId,
    "MAINFRAME_PAYROLL",
    "payroll_data_20241201.dat",
    "DAILY_PAYROLL_TRANSFER",
    "/mainframe/export/payroll",
    "/rhel/landing/payroll",
    "PAYROLL_BATCH_001",
    AuditStatus.SUCCESS,
    "Payroll file transferred successfully",
    AuditDetails.builder()
        .fileSizeBytes(2048576L)
        .fileHashSha256("abc123def456...")
        .recordCount(15000L)
        .build()
);
```

### SQL*Loader Example
```java
auditService.logSqlLoaderOperation(
    correlationId,
    "MAINFRAME_PAYROLL",
    "PAYROLL_STAGING",
    "SQLLOADER_PAYROLL_LOAD",
    "payroll_data_20241201.dat",
    "PAYROLL_STAGING",
    "PAYROLL_BATCH_001",
    AuditStatus.SUCCESS,
    "Payroll data loaded successfully",
    AuditDetails.builder()
        .rowsRead(15000L)
        .rowsLoaded(14950L)
        .rowsRejected(50L)
        .controlTotalAmount(new BigDecimal("2500000.00"))
        .build()
);
```

## Integration with Correlation ID Management

All checkpoint methods integrate seamlessly with the `CorrelationIdManager` to ensure proper correlation ID propagation and thread-local storage management across the entire audit trail.

## Next Steps

With the checkpoint-specific logging methods complete, the next phase focuses on:
1. **REST API Layer** (Tasks 34-40): Dashboard endpoints and API documentation
2. **Security Configuration** (Tasks 41-44): Authentication and authorization
3. **End-to-End Testing** (Task 45): Complete integration test suite