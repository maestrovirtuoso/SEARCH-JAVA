package com.company.search.service;

import com.company.search.model.dto.SearchRequest;
import com.company.search.model.dto.SearchResponse;
import com.company.search.model.dto.SearchResult;
import com.company.search.repository.SearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SearchRepository searchRepository;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(searchRepository);
    }

    @Test
    void search_ShouldReturnResults() {
        // Given
        SearchRequest request = new SearchRequest("test query");
        SearchResult result1 = new SearchResult();
        SearchResult result2 = new SearchResult();
        
        when(searchRepository.search(any(SearchRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(Arrays.asList(result1, result2)));
        when(searchRepository.count(anyString()))
            .thenReturn(CompletableFuture.completedFuture(2L));

        // When
        CompletableFuture<SearchResponse<SearchResult>> result = searchService.search(request);

        // Then
        assertNotNull(result);
        SearchResponse<SearchResult> response = result.join();
        assertEquals(2, response.getResults().size());
        assertEquals(2L, response.getTotalHits());
    }

    @Test
    void searchSimple_ShouldCreateCorrectRequest() {
        // Given
        String query = "simple query";
        int page = 0;
        int size = 10;
        
        when(searchRepository.search(any(SearchRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(Arrays.asList()));
        when(searchRepository.count(anyString()))
            .thenReturn(CompletableFuture.completedFuture(0L));

        // When
        searchService.searchSimple(query, page, size);

        // Then
        verify(searchRepository).search(argThat(request -> 
            query.equals(request.getQuery()) && 
            page == request.getPage() && 
            size == request.getSize()
        ));
    }
}