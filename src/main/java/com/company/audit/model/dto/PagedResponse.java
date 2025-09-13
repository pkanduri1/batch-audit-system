package com.company.audit.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Generic paginated response wrapper for REST API endpoints.
 * Provides pagination metadata along with the actual data content.
 * 
 * @param <T> the type of content in the page
 */
@Schema(description = "Paginated response containing data and pagination metadata")
public class PagedResponse<T> {
    
    @Schema(description = "The actual data content for this page")
    private List<T> content;
    
    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;
    
    @Schema(description = "Number of items per page", example = "20")
    private int size;
    
    @Schema(description = "Total number of elements across all pages", example = "150")
    private long totalElements;
    
    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;
    
    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
    
    @Schema(description = "Number of elements in current page", example = "20")
    private int numberOfElements;
    
    /**
     * Default constructor
     */
    public PagedResponse() {
    }
    
    /**
     * Constructor with all fields
     */
    public PagedResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = page == 0;
        this.last = page >= totalPages - 1;
        this.numberOfElements = content != null ? content.size() : 0;
    }
    
    /**
     * Static factory method to create a PagedResponse
     */
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        return new PagedResponse<>(content, page, size, totalElements);
    }
    
    // Getters and setters
    
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
        this.numberOfElements = content != null ? content.size() : 0;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
        this.first = page == 0;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.last = page >= totalPages - 1;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.last = page >= totalPages - 1;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public boolean isLast() {
        return last;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    public int getNumberOfElements() {
        return numberOfElements;
    }
    
    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
}