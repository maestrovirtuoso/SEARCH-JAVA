package com.company.search.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public class SearchRequest {
    @NotBlank(message = "Query is required")
    @Size(min = 1, max = 1000, message = "Query must be between 1 and 1000 characters")
    private String query;
    
    private List<String> fields;
    
    private Map<String, Object> filters;
    
    @Min(value = 0, message = "Page must be >= 0")
    private int page = 0;
    
    @Min(value = 1, message = "Size must be >= 1")
    private int size = 10;
    
    private String sortBy;
    private String sortOrder = "desc";
    
    // Constructors
    public SearchRequest() {}
    
    public SearchRequest(String query) {
        this.query = query;
    }
    
    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public List<String> getFields() { return fields; }
    public void setFields(List<String> fields) { this.fields = fields; }
    
    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    
    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
}