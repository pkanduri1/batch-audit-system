package com.company.audit.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * AuditDetails POJO for JSON metadata stored in audit events.
 * This class contains detailed information about file metadata, SQL loader statistics,
 * record counts, control totals, and business rule input/output data.
 * 
 * Requirements addressed:
 * - 2.4: Store details_json field for audit events
 * - 3.1: Log file size and hash for integrity verification
 * - 3.2: Capture and log rows read, loaded, and rejected counts
 * - 3.3: Log record counts before and after transformations
 * - 3.4: Log record counts and control totals for reconciliation
 * - 4.2: Capture input and output values in the details_json field
 * - 4.3: Log module name, rule applied, and entity identifier
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "fileSizeBytes", "fileHashSha256", 
    "rowsRead", "rowsLoaded", "rowsRejected",
    "recordCount", "recordCountBefore", "recordCountAfter",
    "controlTotalDebits", "controlTotalCredits", "controlTotalAmount",
    "ruleApplied", "entityIdentifier", "transformationDetails",
    "ruleInput", "ruleOutput"
})
@JsonDeserialize(builder = AuditDetails.Builder.class)
public class AuditDetails {
    
    // File metadata fields (Requirement 3.1)
    @JsonProperty("fileSizeBytes")
    private Long fileSizeBytes;
    
    @JsonProperty("fileHashSha256")
    private String fileHashSha256;
    
    // SQL loader statistics fields (Requirement 3.2)
    @JsonProperty("rowsRead")
    private Long rowsRead;
    
    @JsonProperty("rowsLoaded")
    private Long rowsLoaded;
    
    @JsonProperty("rowsRejected")
    private Long rowsRejected;
    
    // Record count and control total fields (Requirements 3.3, 3.4)
    @JsonProperty("recordCount")
    private Long recordCount;
    
    @JsonProperty("recordCountBefore")
    private Long recordCountBefore;
    
    @JsonProperty("recordCountAfter")
    private Long recordCountAfter;
    
    @JsonProperty("controlTotalDebits")
    private BigDecimal controlTotalDebits;
    
    @JsonProperty("controlTotalCredits")
    private BigDecimal controlTotalCredits;
    
    @JsonProperty("controlTotalAmount")
    private BigDecimal controlTotalAmount;
    
    // Business rule input/output data fields (Requirements 4.2, 4.3)
    @JsonProperty("ruleInput")
    private Map<String, Object> ruleInput;
    
    @JsonProperty("ruleOutput")
    private Map<String, Object> ruleOutput;
    
    @JsonProperty("ruleApplied")
    private String ruleApplied;
    
    @JsonProperty("entityIdentifier")
    private String entityIdentifier;
    
    @JsonProperty("transformationDetails")
    private String transformationDetails;
    
    /**
     * Default constructor
     */
    public AuditDetails() {
    }
    
    /**
     * Constructor for builder pattern
     * @param builder the builder instance
     */
    private AuditDetails(Builder builder) {
        this.fileSizeBytes = builder.fileSizeBytes;
        this.fileHashSha256 = builder.fileHashSha256;
        this.rowsRead = builder.rowsRead;
        this.rowsLoaded = builder.rowsLoaded;
        this.rowsRejected = builder.rowsRejected;
        this.recordCount = builder.recordCount;
        this.recordCountBefore = builder.recordCountBefore;
        this.recordCountAfter = builder.recordCountAfter;
        this.controlTotalDebits = builder.controlTotalDebits;
        this.controlTotalCredits = builder.controlTotalCredits;
        this.controlTotalAmount = builder.controlTotalAmount;
        this.ruleInput = builder.ruleInput;
        this.ruleOutput = builder.ruleOutput;
        this.ruleApplied = builder.ruleApplied;
        this.entityIdentifier = builder.entityIdentifier;
        this.transformationDetails = builder.transformationDetails;
    }
    
    /**
     * Creates a new builder instance
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Gets the file size in bytes
     * @return the file size in bytes
     */
    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }
    
    /**
     * Sets the file size in bytes
     * @param fileSizeBytes the file size in bytes
     */
    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }
    
    /**
     * Gets the file hash (SHA-256)
     * @return the file hash as SHA-256 string
     */
    public String getFileHashSha256() {
        return fileHashSha256;
    }
    
    /**
     * Sets the file hash (SHA-256)
     * @param fileHashSha256 the file hash as SHA-256 string
     */
    public void setFileHashSha256(String fileHashSha256) {
        this.fileHashSha256 = fileHashSha256;
    }
    
    /**
     * Gets the number of rows read during SQL loader operation
     * @return the number of rows read
     */
    public Long getRowsRead() {
        return rowsRead;
    }
    
    /**
     * Sets the number of rows read during SQL loader operation
     * @param rowsRead the number of rows read
     */
    public void setRowsRead(Long rowsRead) {
        this.rowsRead = rowsRead;
    }
    
    /**
     * Gets the number of rows loaded during SQL loader operation
     * @return the number of rows loaded
     */
    public Long getRowsLoaded() {
        return rowsLoaded;
    }
    
    /**
     * Sets the number of rows loaded during SQL loader operation
     * @param rowsLoaded the number of rows loaded
     */
    public void setRowsLoaded(Long rowsLoaded) {
        this.rowsLoaded = rowsLoaded;
    }
    
    /**
     * Gets the number of rows rejected during SQL loader operation
     * @return the number of rows rejected
     */
    public Long getRowsRejected() {
        return rowsRejected;
    }
    
    /**
     * Sets the number of rows rejected during SQL loader operation
     * @param rowsRejected the number of rows rejected
     */
    public void setRowsRejected(Long rowsRejected) {
        this.rowsRejected = rowsRejected;
    }
    
    /**
     * Gets the record count
     * @return the record count
     */
    public Long getRecordCount() {
        return recordCount;
    }
    
    /**
     * Sets the record count
     * @param recordCount the record count
     */
    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }
    
    /**
     * Gets the record count before transformation
     * @return the record count before transformation
     */
    public Long getRecordCountBefore() {
        return recordCountBefore;
    }
    
    /**
     * Sets the record count before transformation
     * @param recordCountBefore the record count before transformation
     */
    public void setRecordCountBefore(Long recordCountBefore) {
        this.recordCountBefore = recordCountBefore;
    }
    
    /**
     * Gets the record count after transformation
     * @return the record count after transformation
     */
    public Long getRecordCountAfter() {
        return recordCountAfter;
    }
    
    /**
     * Sets the record count after transformation
     * @param recordCountAfter the record count after transformation
     */
    public void setRecordCountAfter(Long recordCountAfter) {
        this.recordCountAfter = recordCountAfter;
    }
    
    /**
     * Gets the control total for debits
     * @return the control total for debits
     */
    public BigDecimal getControlTotalDebits() {
        return controlTotalDebits;
    }
    
    /**
     * Sets the control total for debits
     * @param controlTotalDebits the control total for debits
     */
    public void setControlTotalDebits(BigDecimal controlTotalDebits) {
        this.controlTotalDebits = controlTotalDebits;
    }
    
    /**
     * Gets the control total for credits
     * @return the control total for credits
     */
    public BigDecimal getControlTotalCredits() {
        return controlTotalCredits;
    }
    
    /**
     * Sets the control total for credits
     * @param controlTotalCredits the control total for credits
     */
    public void setControlTotalCredits(BigDecimal controlTotalCredits) {
        this.controlTotalCredits = controlTotalCredits;
    }
    
    /**
     * Gets the control total amount
     * @return the control total amount
     */
    public BigDecimal getControlTotalAmount() {
        return controlTotalAmount;
    }
    
    /**
     * Sets the control total amount
     * @param controlTotalAmount the control total amount
     */
    public void setControlTotalAmount(BigDecimal controlTotalAmount) {
        this.controlTotalAmount = controlTotalAmount;
    }
    
    /**
     * Gets the business rule input data
     * @return the business rule input data as a map
     */
    public Map<String, Object> getRuleInput() {
        return ruleInput;
    }
    
    /**
     * Sets the business rule input data
     * @param ruleInput the business rule input data as a map
     */
    public void setRuleInput(Map<String, Object> ruleInput) {
        this.ruleInput = ruleInput;
    }
    
    /**
     * Gets the business rule output data
     * @return the business rule output data as a map
     */
    public Map<String, Object> getRuleOutput() {
        return ruleOutput;
    }
    
    /**
     * Sets the business rule output data
     * @param ruleOutput the business rule output data as a map
     */
    public void setRuleOutput(Map<String, Object> ruleOutput) {
        this.ruleOutput = ruleOutput;
    }
    
    /**
     * Gets the name of the business rule that was applied
     * @return the name of the business rule applied
     */
    public String getRuleApplied() {
        return ruleApplied;
    }
    
    /**
     * Sets the name of the business rule that was applied
     * @param ruleApplied the name of the business rule applied
     */
    public void setRuleApplied(String ruleApplied) {
        this.ruleApplied = ruleApplied;
    }
    
    /**
     * Gets the entity identifier (e.g., account number) for record-level tracing
     * @return the entity identifier
     */
    public String getEntityIdentifier() {
        return entityIdentifier;
    }
    
    /**
     * Sets the entity identifier (e.g., account number) for record-level tracing
     * @param entityIdentifier the entity identifier
     */
    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }
    
    /**
     * Gets the transformation details
     * @return the transformation details
     */
    public String getTransformationDetails() {
        return transformationDetails;
    }
    
    /**
     * Sets the transformation details
     * @param transformationDetails the transformation details
     */
    public void setTransformationDetails(String transformationDetails) {
        this.transformationDetails = transformationDetails;
    }
    
    @Override
    public String toString() {
        return "AuditDetails{" +
                "fileSizeBytes=" + fileSizeBytes +
                ", fileHashSha256='" + fileHashSha256 + '\'' +
                ", rowsRead=" + rowsRead +
                ", rowsLoaded=" + rowsLoaded +
                ", rowsRejected=" + rowsRejected +
                ", recordCount=" + recordCount +
                ", recordCountBefore=" + recordCountBefore +
                ", recordCountAfter=" + recordCountAfter +
                ", controlTotalDebits=" + controlTotalDebits +
                ", controlTotalCredits=" + controlTotalCredits +
                ", controlTotalAmount=" + controlTotalAmount +
                ", ruleInput=" + ruleInput +
                ", ruleOutput=" + ruleOutput +
                ", ruleApplied='" + ruleApplied + '\'' +
                ", entityIdentifier='" + entityIdentifier + '\'' +
                ", transformationDetails='" + transformationDetails + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditDetails that = (AuditDetails) o;
        return Objects.equals(fileSizeBytes, that.fileSizeBytes) &&
                Objects.equals(fileHashSha256, that.fileHashSha256) &&
                Objects.equals(rowsRead, that.rowsRead) &&
                Objects.equals(rowsLoaded, that.rowsLoaded) &&
                Objects.equals(rowsRejected, that.rowsRejected) &&
                Objects.equals(recordCount, that.recordCount) &&
                Objects.equals(recordCountBefore, that.recordCountBefore) &&
                Objects.equals(recordCountAfter, that.recordCountAfter) &&
                Objects.equals(controlTotalDebits, that.controlTotalDebits) &&
                Objects.equals(controlTotalCredits, that.controlTotalCredits) &&
                Objects.equals(controlTotalAmount, that.controlTotalAmount) &&
                Objects.equals(ruleInput, that.ruleInput) &&
                Objects.equals(ruleOutput, that.ruleOutput) &&
                Objects.equals(ruleApplied, that.ruleApplied) &&
                Objects.equals(entityIdentifier, that.entityIdentifier) &&
                Objects.equals(transformationDetails, that.transformationDetails);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fileSizeBytes, fileHashSha256, rowsRead, rowsLoaded, 
                rowsRejected, recordCount, recordCountBefore, recordCountAfter,
                controlTotalDebits, controlTotalCredits, controlTotalAmount,
                ruleInput, ruleOutput, ruleApplied, entityIdentifier, transformationDetails);
    }
    
    /**
     * Builder class for AuditDetails with Jackson support for JSON deserialization.
     * Supports fluent API for easy construction and is compatible with Jackson 2.15+ features.
     */
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        private Long fileSizeBytes;
        private String fileHashSha256;
        private Long rowsRead;
        private Long rowsLoaded;
        private Long rowsRejected;
        private Long recordCount;
        private Long recordCountBefore;
        private Long recordCountAfter;
        private BigDecimal controlTotalDebits;
        private BigDecimal controlTotalCredits;
        private BigDecimal controlTotalAmount;
        private Map<String, Object> ruleInput;
        private Map<String, Object> ruleOutput;
        private String ruleApplied;
        private String entityIdentifier;
        private String transformationDetails;
        
        /**
         * Sets the file size in bytes
         * @param fileSizeBytes the file size in bytes
         * @return this builder instance
         */
        @JsonProperty("fileSizeBytes")
        public Builder fileSizeBytes(Long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
            return this;
        }
        
        /**
         * Sets the file hash (SHA-256)
         * @param fileHashSha256 the file hash as SHA-256 string
         * @return this builder instance
         */
        @JsonProperty("fileHashSha256")
        public Builder fileHashSha256(String fileHashSha256) {
            this.fileHashSha256 = fileHashSha256;
            return this;
        }
        
        /**
         * Sets the number of rows read during SQL loader operation
         * @param rowsRead the number of rows read
         * @return this builder instance
         */
        @JsonProperty("rowsRead")
        public Builder rowsRead(Long rowsRead) {
            this.rowsRead = rowsRead;
            return this;
        }
        
        /**
         * Sets the number of rows loaded during SQL loader operation
         * @param rowsLoaded the number of rows loaded
         * @return this builder instance
         */
        @JsonProperty("rowsLoaded")
        public Builder rowsLoaded(Long rowsLoaded) {
            this.rowsLoaded = rowsLoaded;
            return this;
        }
        
        /**
         * Sets the number of rows rejected during SQL loader operation
         * @param rowsRejected the number of rows rejected
         * @return this builder instance
         */
        @JsonProperty("rowsRejected")
        public Builder rowsRejected(Long rowsRejected) {
            this.rowsRejected = rowsRejected;
            return this;
        }
        
        /**
         * Sets the record count
         * @param recordCount the record count
         * @return this builder instance
         */
        @JsonProperty("recordCount")
        public Builder recordCount(Long recordCount) {
            this.recordCount = recordCount;
            return this;
        }
        
        /**
         * Sets the record count before transformation
         * @param recordCountBefore the record count before transformation
         * @return this builder instance
         */
        @JsonProperty("recordCountBefore")
        public Builder recordCountBefore(Long recordCountBefore) {
            this.recordCountBefore = recordCountBefore;
            return this;
        }
        
        /**
         * Sets the record count after transformation
         * @param recordCountAfter the record count after transformation
         * @return this builder instance
         */
        @JsonProperty("recordCountAfter")
        public Builder recordCountAfter(Long recordCountAfter) {
            this.recordCountAfter = recordCountAfter;
            return this;
        }
        
        /**
         * Sets the control total for debits
         * @param controlTotalDebits the control total for debits
         * @return this builder instance
         */
        @JsonProperty("controlTotalDebits")
        public Builder controlTotalDebits(BigDecimal controlTotalDebits) {
            this.controlTotalDebits = controlTotalDebits;
            return this;
        }
        
        /**
         * Sets the control total for credits
         * @param controlTotalCredits the control total for credits
         * @return this builder instance
         */
        @JsonProperty("controlTotalCredits")
        public Builder controlTotalCredits(BigDecimal controlTotalCredits) {
            this.controlTotalCredits = controlTotalCredits;
            return this;
        }
        
        /**
         * Sets the control total amount
         * @param controlTotalAmount the control total amount
         * @return this builder instance
         */
        @JsonProperty("controlTotalAmount")
        public Builder controlTotalAmount(BigDecimal controlTotalAmount) {
            this.controlTotalAmount = controlTotalAmount;
            return this;
        }
        
        /**
         * Sets the business rule input data
         * @param ruleInput the business rule input data as a map
         * @return this builder instance
         */
        @JsonProperty("ruleInput")
        public Builder ruleInput(Map<String, Object> ruleInput) {
            this.ruleInput = ruleInput;
            return this;
        }
        
        /**
         * Sets the business rule output data
         * @param ruleOutput the business rule output data as a map
         * @return this builder instance
         */
        @JsonProperty("ruleOutput")
        public Builder ruleOutput(Map<String, Object> ruleOutput) {
            this.ruleOutput = ruleOutput;
            return this;
        }
        
        /**
         * Sets the name of the business rule that was applied
         * @param ruleApplied the name of the business rule applied
         * @return this builder instance
         */
        @JsonProperty("ruleApplied")
        public Builder ruleApplied(String ruleApplied) {
            this.ruleApplied = ruleApplied;
            return this;
        }
        
        /**
         * Sets the entity identifier (e.g., account number) for record-level tracing
         * @param entityIdentifier the entity identifier
         * @return this builder instance
         */
        @JsonProperty("entityIdentifier")
        public Builder entityIdentifier(String entityIdentifier) {
            this.entityIdentifier = entityIdentifier;
            return this;
        }
        
        /**
         * Sets the transformation details
         * @param transformationDetails the transformation details
         * @return this builder instance
         */
        @JsonProperty("transformationDetails")
        public Builder transformationDetails(String transformationDetails) {
            this.transformationDetails = transformationDetails;
            return this;
        }
        
        /**
         * Builds the AuditDetails instance
         * @return a new AuditDetails instance
         */
        public AuditDetails build() {
            return new AuditDetails(this);
        }
    }
}