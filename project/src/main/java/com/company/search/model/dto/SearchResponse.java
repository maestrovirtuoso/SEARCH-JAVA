package com.company.search.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class SearchResponse<T> {
    private List<T> results;
    private long totalHits;
    private int page;
    private int size;
    private long searchTime;
    private Map<String, Object> aggregations;
    private Instant timestamp;
    
    public SearchResponse() {
        this.timestamp = Instant.now();
    }
    
    public SearchResponse(List<T> results, long totalHits, int page, int size) {
        this.results = results;
        this.totalHits = totalHits;
        this.page = page;
        this.size = size;
        this.timestamp = Instant.now();
    }
    
    // Getters and Setters
    public List<T> getResults() { return results; }
    public void setResults(List<T> results) { this.results = results; }
    
    public long getTotalHits() { return totalHits; }
    public void setTotalHits(long totalHits) { this.totalHits = totalHits; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public long getSearchTime() { return searchTime; }
    public void setSearchTime(long searchTime) { this.searchTime = searchTime; }
    
    public Map<String, Object> getAggregations() { return aggregations; }
    public void setAggregations(Map<String, Object> aggregations) { this.aggregations = aggregations; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}