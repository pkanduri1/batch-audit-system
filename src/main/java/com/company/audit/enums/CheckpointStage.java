package com.company.audit.enums;

/**
 * Enumeration representing the different checkpoint stages in the data processing pipeline.
 * Each checkpoint corresponds to a critical transition point where audit events are captured.
 * 
 * Based on Requirements 7.1, 7.2, 7.3, 7.4:
 * - Checkpoint 1: Files transfer from mainframe to RHEL
 * - Checkpoint 2: SQL*Loader ingests data to Oracle (start and complete)
 * - Checkpoint 3: Java modules apply business logic
 * - Checkpoint 4: Final files are generated
 */
public enum CheckpointStage {
    
    /**
     * Checkpoint 1: Files transfer from mainframe to RHEL landing area
     */
    RHEL_LANDING("RHEL_LANDING"),
    
    /**
     * Checkpoint 2 Start: SQL*Loader operation begins
     */
    SQLLOADER_START("SQLLOADER_START"),
    
    /**
     * Checkpoint 2 Complete: SQL*Loader operation completes
     */
    SQLLOADER_COMPLETE("SQLLOADER_COMPLETE"),
    
    /**
     * Checkpoint 3: Java modules apply business logic and transformations
     */
    LOGIC_APPLIED("LOGIC_APPLIED"),
    
    /**
     * Checkpoint 4: Final output files are generated
     */
    FILE_GENERATED("FILE_GENERATED");
    
    private final String value;
    
    CheckpointStage(String value) {
        this.value = value;
    }
    
    /**
     * Gets the string value of the checkpoint stage
     * @return the string representation of the checkpoint stage
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Gets the CheckpointStage enum from its string value
     * @param value the string value to convert
     * @return the corresponding CheckpointStage enum
     * @throws IllegalArgumentException if the value doesn't match any checkpoint stage
     */
    public static CheckpointStage fromValue(String value) {
        for (CheckpointStage stage : CheckpointStage.values()) {
            if (stage.value.equals(value)) {
                return stage;
            }
        }
        throw new IllegalArgumentException("Unknown checkpoint stage: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}