package com.company.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Batch Audit System.
 * 
 * This application provides comprehensive end-to-end audit trail functionality
 * for enterprise data processing pipelines, tracking data lineage from multiple
 * mainframe source systems through Oracle staging databases and Java module
 * transformations.
 */
@SpringBootApplication
public class BatchAuditApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchAuditApplication.class, args);
    }
}