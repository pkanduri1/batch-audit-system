# Product Overview

## Batch Audit System

A comprehensive end-to-end audit trail system for enterprise data processing pipelines. The system tracks data lineage from multiple mainframe source systems through Oracle staging databases, Java module transformations, and final output file generation.

## Core Purpose

- **Traceability**: Complete immutable record of data movement across all pipeline stages
- **Data Integrity**: Verification of record counts, control totals, and consistency between systems
- **Error Diagnosis**: Rapid identification of issues at specific modules, sources, and processing stages
- **Compliance**: Reliable audit trail for internal and external regulatory requirements

## Key Features

- Multi-source system support with distinct audit trails
- Checkpoint-based logging at critical pipeline transition points
- REST API dashboard for real-time monitoring and historical analysis
- Automated reconciliation reports with discrepancy detection
- Oracle database persistence with optimized indexing for audit queries
- Spring Boot architecture with comprehensive error handling and resilience

## Target Users

- Data engineers monitoring pipeline health and data flow
- System administrators managing audit data and system performance
- Business analysts verifying data integrity and reconciliation
- Compliance officers requiring audit trails for regulatory purposes
- Support engineers troubleshooting pipeline issues and data discrepancies