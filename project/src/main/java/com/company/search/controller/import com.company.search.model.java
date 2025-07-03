import com.company.search.model.SearchDocument;
import com.company.search.service.DataIndexingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.http.RequestEntity.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

package com.company.search.controller;




@WebMvcTest(DataIndexingController.class)
class DataIndexingControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private DataIndexingService dataIndexingService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void indexNewDocuments_ShouldReturnSuccess() throws Exception {
                // Given
                doNothing().when(dataIndexingService).indexNewDocuments();

                // When & Then
                mockMvc.perform(post("/api/indexing/index-new")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Indexing of new documents started successfully"));

                verify(dataIndexingService, times(1)).indexNewDocuments();
        }

        @Test
        void indexNewDocuments_WhenExceptionThrown_ShouldReturnError() throws Exception {
                // Given
                doThrow(new RuntimeException("Test error")).when(dataIndexingService).indexNewDocuments();

                // When & Then
                mockMvc.perform(post("/api/indexing/index-new")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Failed to start indexing: Test error"));
        }

        @Test
        void indexDocumentsByCategory_ShouldReturnSuccess() throws Exception {
                // Given
                String category = "testCategory";
                when(dataIndexingService.indexDocumentsByCategory(anyString()))
                                .thenReturn(CompletableFuture.completedFuture(true));

                // When & Then
                mockMvc.perform(post("/api/indexing/index-by-category")
                                .param("category", category)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Documents for category 'testCategory' indexed successfully"));

                verify(dataIndexingService, times(1)).indexDocumentsByCategory(category);
        }

        @Test
        void indexDocumentsByCategory_WhenExceptionThrown_ShouldReturnError() throws Exception {
                // Given
                String category = "testCategory";
                when(dataIndexingService.indexDocumentsByCategory(anyString()))
                                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test error")));

                // When & Then
                mockMvc.perform(post("/api/indexing/index-by-category")
                                .param("category", category)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Failed to index documents for category 'testCategory': Test error"));
        }

        @Test
        void indexSingleDocument_ShouldReturnSuccess() throws Exception {
                // Given
                SearchDocument document = new SearchDocument();
                document.setId("doc-123");
                document.setTitle("Test Document");
                document.setContent("Test content");

                when(dataIndexingService.indexSingleDocument(any(SearchDocument.class)))
                                .thenReturn(CompletableFuture.completedFuture(true));

                // When & Then
                mockMvc.perform(post("/api/indexing/index-document")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(document)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Document 'doc-123' indexed successfully"));

                verify(dataIndexingService, times(1)).indexSingleDocument(any(SearchDocument.class));
        }

        @Test
        void indexSingleDocument_WhenExceptionThrown_ShouldReturnError() throws Exception {
                // Given
                SearchDocument document = new SearchDocument();
                document.setId("doc-123");
                document.setTitle("Test Document");
                document.setContent("Test content");

                when(dataIndexingService.indexSingleDocument(any(SearchDocument.class)))
                                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test error")));

                // When & Then
                mockMvc.perform(post("/api/indexing/index-document")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(document)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Failed to index document 'doc-123': Test error"));
        }

        @Test
        void reindexAllDocuments_ShouldReturnSuccess() throws Exception {
                // Given
                when(dataIndexingService.reindexAllDocuments())
                                .thenReturn(CompletableFuture.completedFuture(true));

                // When & Then
                mockMvc.perform(post("/api/indexing/reindex-all")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Full reindexing completed successfully"));

                verify(dataIndexingService, times(1)).reindexAllDocuments();
        }

        @Test
        void reindexAllDocuments_WhenExceptionThrown_ShouldReturnError() throws Exception {
                // Given
                when(dataIndexingService.reindexAllDocuments())
                                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test error")));

                // When & Then
                mockMvc.perform(post("/api/indexing/reindex-all")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Failed to complete full reindexing: Test error"));
        }

        @Test
        void healthCheck_ShouldReturnHealthyStatus() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/indexing/health")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("healthy"))
                                .andExpect(jsonPath("$.service").value("DataIndexingService"))
                                .andExpect(jsonPath("$.message").value("Indexing service is running normally"));
        }
}