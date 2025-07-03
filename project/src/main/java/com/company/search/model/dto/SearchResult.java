package com.company.search.model.dto;

import com.company.search.model.SearchDocument;

public class SearchResult {
    private SearchDocument document;
    private float score;
    private String[] highlight;
    
    public SearchResult() {}
    
    public SearchResult(SearchDocument document, float score) {
        this.document = document;
        this.score = score;
    }
    
    // Getters and Setters
    public SearchDocument getDocument() { return document; }
    public void setDocument(SearchDocument document) { this.document = document; }
    
    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }
    
    public String[] getHighlight() { return highlight; }
    public void setHighlight(String[] highlight) { this.highlight = highlight; }
}