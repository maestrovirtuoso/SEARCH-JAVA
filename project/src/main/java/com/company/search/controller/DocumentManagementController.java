package com.company.search.controller;

import com.company.search.model.SearchDocument;
import com.company.search.repository.DataScyllaRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentManagementController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentManagementController.class);
    private final DataScyllaRepository scyllaRepository;

    public DocumentManagementController(DataScyllaRepository scyllaRepository) {
        this.scyllaRepository = scyllaRepository;
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "DocumentManagementController is working!"
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDocument(@Valid @RequestBody SearchDocument document) {
        try {
            if (document.getId() == null || document.getId().isEmpty()) {
                document.setId(UUID.randomUUID().toString());
            }
            if (document.getCreatedAt() == null) {
                document.setCreatedAt(Instant.now());
            }
            document.setUpdatedAt(Instant.now());

            SearchDocument savedDocument = scyllaRepository.save(document);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Document created successfully",
                    "document", savedDocument
            ));
        } catch (Exception e) {
            logger.error("Error creating document", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to create document: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countDocuments() {
        try {
            long count = scyllaRepository.count();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "totalDocuments", count
            ));
        } catch (Exception e) {
            logger.error("Error counting documents", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to count documents: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/debug/jackson")
    public ResponseEntity<Map<String, Object>> debugJackson() {
        try {
            // Récupérer un document de ScyllaDB
            List<SearchDocument> documents = scyllaRepository.findAll();
            if (!documents.isEmpty()) {
                SearchDocument doc = documents.get(0);

                // Afficher le document
                logger.info("Document retrieved: {}", doc);
                logger.info("Document ID: {}", doc.getId());
                logger.info("Document title: {}", doc.getTitle());
                logger.info("Document createdAt: {}", doc.getCreatedAt());
                logger.info("Document metadata: {}", doc.getMetadata());

                // Tester la sérialisation JSON
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

                String json = objectMapper.writeValueAsString(doc);

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "document", doc,
                        "json", json,
                        "message", "Serialization successful"
                ));
            }

            return ResponseEntity.ok(Map.of("status", "no documents"));

        } catch (Exception e) {
            logger.error("Jackson debug error: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "cause", e.getCause() != null ? e.getCause().getMessage() : "No cause"
            ));
        }
    }

}