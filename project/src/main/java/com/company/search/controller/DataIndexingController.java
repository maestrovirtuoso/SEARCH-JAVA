package com.company.search.controller;

import com.company.search.model.SearchDocument;
import com.company.search.service.DataIndexingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/indexing")
@CrossOrigin(origins = "*")
public class DataIndexingController {

    private static final Logger logger = LoggerFactory.getLogger(DataIndexingController.class);

    private final DataIndexingService dataIndexingService;

    public DataIndexingController(DataIndexingService dataIndexingService) {
        this.dataIndexingService = dataIndexingService;
    }

    /**
     * Déclenche l'indexation manuelle de tous les nouveaux documents
     */
    @PostMapping("/index-new")
    public ResponseEntity<Map<String, String>> indexNewDocuments() {
        logger.info("Manual indexing of new documents requested");

        try {
            dataIndexingService.indexNewDocuments();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Indexing of new documents started successfully"
            ));
        } catch (Exception e) {
            logger.error("Error triggering manual indexing", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to start indexing: " + e.getMessage()
            ));
        }
    }

    /**
     * Indexe tous les documents d'une catégorie spécifique de manière asynchrone
     */
    @PostMapping("/index-by-category")
    public CompletableFuture<ResponseEntity<Map<String, String>>> indexDocumentsByCategory(
            @RequestParam String category) {

        logger.info("Indexing requested for category: {}", category);

        return dataIndexingService.indexDocumentsByCategory(category)
                .thenApply(result -> ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Documents for category '" + category + "' indexed successfully"
                )))
                .exceptionally(ex -> {
                    logger.error("Error indexing documents for category: {}", category, ex);
                    return ResponseEntity.internalServerError().body(Map.of(
                            "status", "error",
                            "message", "Failed to index documents for category '" + category + "': " + ex.getMessage()
                    ));
                });
    }

    /**
     * Indexe un document unique de manière asynchrone
     */
    @PostMapping("/index-document")
    public CompletableFuture<ResponseEntity<Map<String, String>>> indexSingleDocument(
            @Valid @RequestBody SearchDocument document) {

        logger.info("Single document indexing requested for document ID: {}", document.getId());

        return dataIndexingService.indexSingleDocument(document)
                .thenApply(result -> ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Document '" + document.getId() + "' indexed successfully"
                )))
                .exceptionally(ex -> {
                    logger.error("Error indexing single document: {}", document.getId(), ex);
                    return ResponseEntity.internalServerError().body(Map.of(
                            "status", "error",
                            "message", "Failed to index document '" + document.getId() + "': " + ex.getMessage()
                    ));
                });
    }

    /**
     * Lance une réindexation complète de tous les documents de manière asynchrone
     */
    @PostMapping("/reindex-all")
    public CompletableFuture<ResponseEntity<Map<String, String>>> reindexAllDocuments() {

        logger.info("Full reindexing requested");

        return dataIndexingService.reindexAllDocuments()
                .thenApply(result -> ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Full reindexing completed successfully"
                )))
                .exceptionally(ex -> {
                    logger.error("Error during full reindexing", ex);
                    return ResponseEntity.internalServerError().body(Map.of(
                            "status", "error",
                            "message", "Failed to complete full reindexing: " + ex.getMessage()
                    ));
                });
    }

    /**
     * Endpoint de santé pour vérifier le statut du service d'indexation
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "DataIndexingService",
                "message", "Indexing service is running normally"
        ));
    }
}