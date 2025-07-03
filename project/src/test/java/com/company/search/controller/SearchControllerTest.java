package com.company.search.controller;

import com.company.search.model.dto.SearchRequest;
import com.company.search.model.dto.SearchResponse;
import com.company.search.model.dto.SearchResult;
import com.company.search.model.SearchDocument;
import com.company.search.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void search_ShouldReturnSearchResults() throws Exception {
        // Given
        SearchRequest request = new SearchRequest("test query");
        SearchDocument testDoc = new SearchDocument();
        testDoc.setId("test-id");
        testDoc.setTitle("Test Document");
        testDoc.setContent("Test content");
        
        SearchResult result = new SearchResult(testDoc, 1.0f);
        SearchResponse<SearchResult> response = new SearchResponse<>(
            Arrays.asList(result), 1L, 0, 10
        );

        when(searchService.search(any(SearchRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(response));

        // When & Then
        mockMvc.perform(post("/api/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalHits").value(1))
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void searchSimple_ShouldReturnResults() throws Exception {
        // Given
        SearchDocument testDoc = new SearchDocument();
        testDoc.setId("test-id");
        testDoc.setTitle("Test Document");
        testDoc.setContent("Test content");
        
        SearchResult result = new SearchResult(testDoc, 1.0f);
        SearchResponse<SearchResult> response = new SearchResponse<>(
            Arrays.asList(result), 1L, 0, 10
        );

        when(searchService.searchSimple("test", 0, 10))
            .thenReturn(CompletableFuture.completedFuture(response));

        // When & Then
        mockMvc.perform(get("/api/search")
                .param("query", "test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalHits").value(1));
    }
}