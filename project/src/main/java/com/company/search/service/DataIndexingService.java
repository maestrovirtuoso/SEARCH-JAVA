package com.company.search.service;

import com.company.search.model.SearchDocument;
import com.company.search.repository.DataScyllaRepository;
import com.company.search.repository.SearchElasticsearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class DataIndexingService {

    private static final Logger logger = LoggerFactory.getLogger(DataIndexingService.class);

    private final DataScyllaRepository scyllaRepository;
    private final SearchElasticsearchRepository elasticsearchRepository;

    @Autowired
    public DataIndexingService(DataScyllaRepository scyllaRepository,
                               SearchElasticsearchRepository elasticsearchRepository) {
        this.scyllaRepository = scyllaRepository;
        this.elasticsearchRepository = elasticsearchRepository;
    }

    @Scheduled(fixedRate = 3600000) // 1 heure
    public void indexNewDocuments() {
        logger.info("Starting scheduled indexing of new documents");

        try {
            // Récupérer tous les documents de ScyllaDB
            List<SearchDocument> documents = scyllaRepository.findAll();
            logger.info("Retrieved {} documents for indexing", documents.size());

            // Indexer les documents dans Elasticsearch
            for (SearchDocument document : documents) {
                try {
                    elasticsearchRepository.indexDocument(document);
                } catch (Exception e) {
                    logger.error("Error indexing document {}: {}", document.getId(), e.getMessage(), e);
                }
            }

            logger.info("Completed indexing of {} documents", documents.size());

        } catch (Exception e) {
            logger.error("Error during scheduled indexing", e);
        }
    }

    @Async
    public CompletableFuture<Void> indexDocumentsByCategory(String category) {
        return CompletableFuture.runAsync(() -> {
            logger.info("Indexing documents for category: {}", category);

            try {
                List<SearchDocument> documents = scyllaRepository.findByCategory(category);
                logger.info("Retrieved {} documents for category {}", documents.size(), category);

                // Indexer chaque document dans Elasticsearch
                for (SearchDocument document : documents) {
                    try {
                        elasticsearchRepository.indexDocument(document);
                    } catch (Exception e) {
                        logger.error("Error indexing document {} in category {}: {}",
                                document.getId(), category, e.getMessage());
                    }
                }

                logger.info("Completed indexing for category {}", category);

            } catch (Exception e) {
                logger.error("Error indexing documents for category {}", category, e);
                throw new RuntimeException("Failed to index documents for category: " + category, e);
            }
        });
    }

    @Async
    public CompletableFuture<Void> indexSingleDocument(SearchDocument document) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Indexing single document: {}", document.getId());
                elasticsearchRepository.indexDocument(document);
                logger.debug("Successfully indexed document: {}", document.getId());
            } catch (Exception e) {
                logger.error("Error indexing document {}: {}", document.getId(), e.getMessage());
                throw new RuntimeException("Failed to index document: " + document.getId(), e);
            }
        });
    }

    @Async
    public CompletableFuture<Void> reindexAllDocuments() {
        return CompletableFuture.runAsync(() -> {
            logger.info("Starting full reindexing of all documents");

            try {
                // Supprimer l'index existant et le recréer
                elasticsearchRepository.deleteIndex();
                elasticsearchRepository.createIndex();

                // Récupérer et indexer tous les documents
                List<SearchDocument> documents = scyllaRepository.findAll();
                logger.info("Found {} documents to reindex", documents.size());

                int successCount = 0;
                int errorCount = 0;

                for (SearchDocument document : documents) {
                    try {
                        elasticsearchRepository.indexDocument(document);
                        successCount++;
                    } catch (Exception e) {
                        logger.error("Error reindexing document {}: {}", document.getId(), e.getMessage());
                        errorCount++;
                    }
                }

                logger.info("Reindexing completed. Success: {}, Errors: {}", successCount, errorCount);

            } catch (Exception e) {
                logger.error("Error during full reindexing", e);
                throw new RuntimeException("Failed to complete reindexing", e);
            }
        });
    }
}