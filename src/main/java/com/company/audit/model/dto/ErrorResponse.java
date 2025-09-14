package com.company.audit.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error response DTO for REST API endpoints.
 * 
 * This class provides a consistent error response structure across all audit API endpoints,
 * including error codes, messages, field-specific validation errors, and additional context.
 * It supports SpringDoc OpenAPI v2 documentation and Jackson JSON serialization.
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response for audit API endpoints")
public class ErrorResponse {

    @JsonProperty("timestamp")
    @Schema(description = "Timestamp when the error occurred", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;

    @JsonProperty("status")
    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @JsonProperty("error")
    @Schema(description = "HTTP status text", example = "Bad Request")
    private String error;

    @JsonProperty("errorCode")
    @Schema(description = "Application-specific error code", example = "AUDIT_VALIDATION_ERROR")
    private String errorCode;

    @JsonProperty("message")
    @Schema(description = "Human-readable error message", example = "Validation failed for audit event")
    private String message;

    @JsonProperty("path")
    @Schema(description = "Request path that caused the error", example = "/api/audit/events")
    private String path;

    @JsonProperty("method")
    @Schema(description = "HTTP method that caused the error", example = "GET")
    private String method;

    @JsonProperty("correlationId")
    @Schema(description = "Request correlation ID for tracing", example = "550e8400-e29b-41d4-a716-446655440000")
    private String correlationId;

    @JsonProperty("fieldErrors")
    @Schema(description = "Field-specific validation errors")
    private List<FieldError> fieldErrors;

    @JsonProperty("details")
    @Schema(description = "Additional error details and context")
    private Map<String, Object> details;

    @JsonProperty("retryable")
    @Schema(description = "Whether the operation can be retried", example = "false")
    private Boolean retryable;

    @JsonProperty("retryAfter")
    @Schema(description = "Suggested retry delay in seconds", example = "30")
    private Integer retryAfter;

    /**
     * Default constructor for JSON deserialization.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with basic error information.
     * 
     * @param status HTTP status code
     * @param error HTTP status text
     * @param message error message
     * @param path request path
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /**
     * Constructor with comprehensive error information.
     * 
     * @param status HTTP status code
     * @param error HTTP status text
     * @param errorCode application-specific error code
     * @param message error message
     * @param path request path
     * @param method HTTP method
     */
    public ErrorResponse(int status, String error, String errorCode, String message, String path, String method) {
        this();
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.method = method;
    }

    // Getters and setters

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public Boolean getRetryable() {
        return retryable;
    }

    public void setRetryable(Boolean retryable) {
        this.retryable = retryable;
    }

    public Integer getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(Integer retryAfter) {
        this.retryAfter = retryAfter;
    }

    /**
     * Nested class for field-specific validation errors.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Field-specific validation error")
    public static class FieldError {

        @JsonProperty("field")
        @Schema(description = "Name of the field that failed validation", example = "correlationId")
        private String field;

        @JsonProperty("rejectedValue")
        @Schema(description = "Value that was rejected", example = "invalid-uuid")
        private Object rejectedValue;

        @JsonProperty("message")
        @Schema(description = "Field-specific error message", example = "Invalid UUID format")
        private String message;

        @JsonProperty("code")
        @Schema(description = "Field-specific error code", example = "INVALID_FORMAT")
        private String code;

        /**
         * Default constructor for JSON deserialization.
         */
        public FieldError() {}

        /**
         * Constructor with field error information.
         * 
         * @param field field name
         * @param rejectedValue rejected value
         * @param message error message
         * @param code error code
         */
        public FieldError(String field, Object rejectedValue, String message, String code) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
            this.code = code;
        }

        // Getters and setters

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}