package com.company.search.repository;

import com.company.search.model.SearchDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public class AsyncDataScyllaRepository {

    @Autowired
    private DataScyllaRepository dataScyllaRepository;

    @Async
    public CompletableFuture<List<SearchDocument>> getAllDocuments(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<SearchDocument> allDocs = dataScyllaRepository.findAll();
            // Limiter le nombre de documents si nécessaire
            if (limit > 0 && allDocs.size() > limit) {
                return allDocs.subList(0, limit);
            }
            return allDocs;
        });
    }

    @Async
    public CompletableFuture<List<SearchDocument>> getDocumentsByCategory(String category, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<SearchDocument> categoryDocs = dataScyllaRepository.findByCategory(category);
            // Limiter le nombre de documents si nécessaire
            if (limit > 0 && categoryDocs.size() > limit) {
                return categoryDocs.subList(0, limit);
            }
            return categoryDocs;
        });
    }

    @Async
    public CompletableFuture<Optional<SearchDocument>> getDocumentById(String id) {
        return CompletableFuture.supplyAsync(() -> dataScyllaRepository.findById(id));
    }

    @Async
    public CompletableFuture<Void> saveDocument(SearchDocument document) {
        return CompletableFuture.runAsync(() -> dataScyllaRepository.save(document));
    }

    @Async
    public CompletableFuture<Void> updateDocument(SearchDocument document) {
        return CompletableFuture.runAsync(() -> dataScyllaRepository.update(document));
    }

    @Async
    public CompletableFuture<Void> deleteDocument(String id) {
        return CompletableFuture.runAsync(() -> dataScyllaRepository.deleteById(id));
    }
}