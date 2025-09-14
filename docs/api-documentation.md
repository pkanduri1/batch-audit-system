# REST API Documentation

## Overview

The Batch Audit System provides a comprehensive REST API for monitoring audit events, generating reconciliation reports, and accessing audit statistics. The API is built with Spring Boot 3.4+ and documented using SpringDoc OpenAPI v2.

## Base URL

```
http://localhost:8080/api/audit
```

## Authentication

All API endpoints require JWT authentication with appropriate roles:
- **AUDIT_USER**: Read access to audit data and reports
- **AUDIT_ADMIN**: Full access including system administration

## API Endpoints

### 1. Audit Events

#### GET /api/audit/events
Retrieve paginated audit events with optional filtering.

**Parameters:**
- `sourceSystem` (optional): Filter by source system identifier
- `moduleName` (optional): Filter by module name
- `status` (optional): Filter by audit status (SUCCESS, FAILURE, WARNING)
- `checkpointStage` (optional): Filter by checkpoint stage
- `page` (default: 0): Page number (0-based)
- `size` (default: 20, max: 1000): Number of items per page

**Response:**
```json
{
  "content": [
    {
      "auditId": "550e8400-e29b-41d4-a716-446655440000",
      "correlationId": "123e4567-e89b-12d3-a456-426614174000",
      "sourceSystem": "MAINFRAME_SYSTEM_A",
      "moduleName": "FILE_TRANSFER",
      "processName": "DAILY_BATCH_PROCESS",
      "sourceEntity": "CUSTOMER_DATA.csv",
      "destinationEntity": "STAGING.CUSTOMER_STAGING",
      "keyIdentifier": "BATCH_20240115_001",
      "checkpointStage": "RHEL_LANDING",
      "eventTimestamp": "2024-01-15T10:30:00",
      "status": "SUCCESS",
      "message": "File successfully transferred from mainframe to RHEL landing zone",
      "detailsJson": "{\"fileSize\": 1024000, \"recordCount\": 5000}"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false,
  "numberOfElements": 20
}
```

### 2. Audit Statistics

#### GET /api/audit/statistics
Generate comprehensive audit statistics for a specified date range.

**Parameters:**
- `startDate` (required): Start date in ISO 8601 format (e.g., "2024-01-01T00:00:00")
- `endDate` (required): End date in ISO 8601 format (e.g., "2024-01-31T23:59:59")

**Response:**
```json
{
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-01-31T23:59:59",
  "totalEvents": 1500,
  "successfulEvents": 1350,
  "failedEvents": 100,
  "warningEvents": 50,
  "successRate": 90.0,
  "failureRate": 6.67,
  "warningRate": 3.33,
  "eventsBySourceSystem": {
    "MAINFRAME_A": 600,
    "MAINFRAME_B": 400,
    "MAINFRAME_C": 500
  },
  "eventsByModule": {
    "FILE_TRANSFER": 500,
    "SQL_LOADER": 400,
    "BUSINESS_RULES": 300,
    "FILE_GENERATOR": 300
  },
  "eventsByCheckpointStage": {
    "RHEL_LANDING": 400,
    "SQLLOADER_COMPLETE": 350,
    "LOGIC_APPLIED": 350,
    "FILE_GENERATED": 400
  },
  "averageEventsPerDay": 48.39,
  "peakEventsPerDay": 75,
  "peakDate": "2024-01-15T00:00:00"
}
```

### 3. Data Discrepancies

#### GET /api/audit/discrepancies
Identify and retrieve data discrepancies with optional filtering.

**Parameters:**
- `sourceSystem` (optional): Filter by source system
- `moduleName` (optional): Filter by module name
- `severity` (optional): Filter by severity (LOW, MEDIUM, HIGH, CRITICAL)
- `status` (optional): Filter by status (OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE, ACKNOWLEDGED)
- `startDate` (optional): Start date for discrepancy detection
- `endDate` (optional): End date for discrepancy detection

**Response:**
```json
[
  {
    "discrepancyId": "550e8400-e29b-41d4-a716-446655440001",
    "correlationId": "123e4567-e89b-12d3-a456-426614174000",
    "sourceSystem": "MAINFRAME_SYSTEM_A",
    "moduleName": "SQL_LOADER",
    "discrepancyType": "RECORD_COUNT_MISMATCH",
    "severity": "MEDIUM",
    "checkpointStage": "SQLLOADER_COMPLETE",
    "expectedValue": "1000",
    "actualValue": "995",
    "difference": "5",
    "description": "Record count mismatch between input and output",
    "keyIdentifier": "BATCH_20240115_001",
    "detectedAt": "2024-01-15T14:30:00",
    "status": "OPEN"
  }
]
```

### 4. Reconciliation Reports

#### GET /api/audit/reconciliation/{correlationId}
Generate a comprehensive reconciliation report for a specific pipeline run.

**Parameters:**
- `correlationId` (path): UUID of the pipeline run

**Response:**
```json
{
  "correlationId": "123e4567-e89b-12d3-a456-426614174000",
  "sourceSystem": "MAINFRAME_SYSTEM_A",
  "reportGeneratedAt": "2024-01-15T15:00:00",
  "pipelineStartTime": "2024-01-15T09:00:00",
  "pipelineEndTime": "2024-01-15T10:25:00",
  "overallStatus": "SUCCESS",
  "checkpointCounts": {
    "RHEL_LANDING": 1000,
    "SQLLOADER_COMPLETE": 950,
    "LOGIC_APPLIED": 950,
    "FILE_GENERATED": 950
  },
  "controlTotals": {
    "RHEL_LANDING": 50000.0,
    "FILE_GENERATED": 50000.0
  },
  "discrepancies": [],
  "summary": {
    "totalRecordsProcessed": 1000,
    "successfulEvents": 950,
    "failedEvents": 50,
    "warningEvents": 0,
    "successRate": 95.0,
    "totalProcessingTimeMs": 5100000,
    "dataIntegrityValid": true
  },
  "checkpointDetails": [
    {
      "checkpointStage": "RHEL_LANDING",
      "recordCount": 1000,
      "controlTotal": 50000.0,
      "status": "SUCCESS",
      "startTime": "2024-01-15T09:00:00",
      "endTime": "2024-01-15T09:15:00",
      "durationMs": 900000
    }
  ]
}
```

#### GET /api/audit/reconciliation/reports
Retrieve all available reconciliation reports with optional filtering.

**Parameters:**
- `sourceSystem` (optional): Filter by source system
- `status` (optional): Filter by overall status (SUCCESS, FAILURE, WARNING)
- `startDate` (optional): Filter by pipeline start date
- `endDate` (optional): Filter by pipeline end date

**Response:**
```json
[
  {
    "correlationId": "123e4567-e89b-12d3-a456-426614174000",
    "sourceSystem": "MAINFRAME_SYSTEM_A",
    "reportGeneratedAt": "2024-01-15T15:00:00",
    "overallStatus": "SUCCESS",
    "summary": {
      "totalRecordsProcessed": 1000,
      "successRate": 95.0,
      "dataIntegrityValid": true
    }
  }
]
```

#### GET /api/audit/reconciliation/{correlationId}/dto
Generate reconciliation report with specified detail level.

**Parameters:**
- `correlationId` (path): UUID of the pipeline run
- `reportType` (query, default: "STANDARD"): Report type (STANDARD, DETAILED, SUMMARY)

**Response varies by report type:**
- **STANDARD**: Essential information with checkpoint counts and basic summary
- **DETAILED**: Comprehensive analysis with all discrepancies and performance metrics
- **SUMMARY**: High-level metrics with processing time and critical issues count

## Error Responses

All endpoints return standardized error responses:

```json
{
  "timestamp": "2024-01-15T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid correlation ID format",
  "path": "/api/audit/reconciliation/invalid-uuid"
}
```

### Common HTTP Status Codes

- **200 OK**: Successful request
- **400 Bad Request**: Invalid parameters or request format
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side error

## Swagger UI

Interactive API documentation is available at:
```
http://localhost:8080/swagger-ui.html
```

The Swagger UI provides:
- Interactive API testing
- Complete parameter documentation
- Response schema definitions
- Authentication configuration
- Example requests and responses

## Rate Limiting

API endpoints are subject to rate limiting:
- **Standard endpoints**: 100 requests per minute per user
- **Statistics endpoints**: 10 requests per minute per user
- **Report generation**: 5 requests per minute per user

## Data Types and Formats

### Enumerations

**AuditStatus:**
- `SUCCESS`: Operation completed successfully
- `FAILURE`: Operation failed with error
- `WARNING`: Operation completed with warnings

**CheckpointStage:**
- `RHEL_LANDING`: Files transferred to RHEL
- `SQLLOADER_START`: SQL*Loader operation begins
- `SQLLOADER_COMPLETE`: SQL*Loader operation completes
- `LOGIC_APPLIED`: Business logic processing
- `FILE_GENERATED`: Final output files created

**DiscrepancySeverity:**
- `LOW`: Minor discrepancy
- `MEDIUM`: Moderate discrepancy
- `HIGH`: Critical discrepancy
- `CRITICAL`: System-wide impact

### Date Format

All timestamps use ISO 8601 format: `yyyy-MM-dd'T'HH:mm:ss`

### UUID Format

All UUIDs follow standard format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`

## Client Libraries

The API can be consumed using standard HTTP clients or generated client libraries from the OpenAPI specification available at:
```
http://localhost:8080/v3/api-docs
```