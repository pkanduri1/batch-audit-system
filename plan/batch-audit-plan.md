# **End-to-End Data Pipeline Audit Plan (Multi-Source Edition)**

## **1\. Introduction & Goals**

This document outlines a strategy for implementing a comprehensive, end-to-end audit trail for a data processing pipeline that ingests files from **multiple source systems** and processes them using **different Java modules**. This plan is designed for the existing Java Spring Boot application and its surrounding systems.

**Primary Goals:**

* **Traceability:** Create a clear, immutable record of data from each source as it moves through every system and transformation stage.  
* **Data Integrity:** Verify record counts, control totals, and data consistency between each mainframe source, the Oracle database, and the final output files.  
* **Error Diagnosis:** Quickly identify the exact module, source, and stage causing issues, discrepancies, or data fallout.  
* **Compliance & Reporting:** Provide a reliable and holistic data source for internal and external audits, with clear lineage for each source system.

## **2\. Audit Checkpoints & Data Flow**

The audit process will inject logging and tracking at key transition points. This flow diagram represents the journey for **each individual source system**.

graph TD  
    subgraph "Mainframe & Transfer"  
        A\[Source Master Files\] \--\>|Checkpoint 1| B{Connect:Direct Transfer};  
    end

    subgraph "RHEL & Staging DB"  
        B \--\> C\[Files land on RHEL\];  
        C \--\>|Checkpoint 2| D{SQL\*Loader};  
        D \--\> E\[(Oracle Staging Tables)\];  
    end

    subgraph "Processing & Transformation (Java Modules)"  
        E \--\>|Checkpoint 3| F{Module A, Module B, ...};  
    end  
      
    subgraph "Final Output"  
        F \--\>|Checkpoint 4| G\[Create Fixed-Width Files\];  
        G \--\> H((End Files Delivered));  
    end

## **3\. Implementation Strategy**

### **Phase 1: Foundational Audit Logging**

The core of the audit trail will be a centralized and dedicated log structure, stored in a separate, indexed database table (e.g., PIPELINE\_AUDIT\_LOG).

**Audit Log Schema:**

* auditId (Primary Key, UUID)  
* correlationId (Links all events for a single process run, e.g., daily batch, UUID)  
* **source\_system (NEW: Identifier for the source, e.g., 'SRC\_SYS\_A', 'SRC\_SYS\_B')**  
* **module\_name (NEW: Name of the Java module processing the data, e.g., 'payment-processor', 'contact-updater')**  
* process\_name (e.g., 'FileTransfer', 'SQLLoader', 'BusinessLogic', 'FileGeneration')  
* source\_entity (Name of the file or table being processed)  
* destination\_entity (Name of the target file or table)  
* key\_identifier (e.g., account\_number, for record-level tracing)  
* checkpoint\_stage (A code for the specific checkpoint, e.g., 'RHEL\_LANDING', 'SQLLOADER\_COMPLETE', 'LOGIC\_APPLIED')  
* event\_timestamp (UTC timestamp)  
* status ('SUCCESS', 'FAILURE', 'WARNING')  
* message (Descriptive log message)  
* details\_json (A JSON blob for context-specific metadata)

### **Phase 2: Checkpoint Implementation**

#### **Checkpoint 1: File Transfer (Mainframe to RHEL)**

**Location:** A script on the RHEL server monitoring for incoming files.

**Actions:**

1. **Generate correlationId:** At the start of a new batch run, generate a unique correlationId for the entire pipeline run.  
2. **Log File Arrival:** For each file that arrives:  
   * source\_system: Identify the source based on filename or directory.  
   * message: File ${source\_entity} from ${source\_system} successfully received.  
   * details\_json: {"file\_size\_bytes": X, "file\_hash\_sha256": "abc..."}

#### **Checkpoint 2: Database Ingestion (RHEL to Oracle)**

**Location:** The script or component that invokes SQL\*Loader.

**Actions:**

1. **Log Pre-Load:** Before starting SQL\*Loader:  
   * source\_system: Carry over from the source file.  
   * message: Starting SQL\*Loader for ${source\_entity} into table ${destination\_entity}.  
2. **Log Post-Load:** After SQL\*Loader completes, parse its log file:  
   * source\_system: Carry over from the source file.  
   * details\_json: {"rows\_read": X, "rows\_loaded": Y, "rows\_rejected": Z}

#### **Checkpoint 3: Business Logic Application (Java/Spring Boot Modules)**

**Location:** **Each core Java service/module** that transforms data.

Actions:  
This checkpoint is mandatory for each module that handles the data.

1. **Log Rule Application:** When a significant business rule is applied to an entity:  
   * source\_system: The source system of the data being processed.  
   * module\_name: The name of the Java module executing the logic.  
   * key\_identifier: The account number or primary entity ID.  
   * message: Rule 'ChargeOffDateCalculation' applied by module ${module\_name}.  
   * details\_json: {"rule\_input": {...}, "rule\_output": {...}}

#### **Checkpoint 4: Final File Generation**

**Location:** The Java module(s) responsible for creating the final fixed-width files.

Actions:  
After each output file is written:

1. **Log File Creation:**  
   * source\_system: The primary source system for the data in the file (or 'multiple' if aggregated).  
   * module\_name: The name of the Java module that generated the file.  
   * destination\_entity: The name of the created file.  
   * details\_json: {"record\_count": X, "control\_total\_debits": Y}

## **4\. Reconciliation & Reporting**

* **End-to-End Reconciliation Report:** At the conclusion of each pipeline run, an automated summary report must be generated. This report will now be **broken down by source\_system**, comparing record counts from source files to Oracle loads and finally to the records in output files for each source.  
* **Audit Dashboard:** The PIPELINE\_AUDIT\_LOG data should be fed into a dashboard (e.g., Kibana, Grafana). This dashboard must now include **filters for source\_system and module\_name**, allowing engineers and analysts to isolate the data flow for a specific source or trace an issue to a particular Java module.