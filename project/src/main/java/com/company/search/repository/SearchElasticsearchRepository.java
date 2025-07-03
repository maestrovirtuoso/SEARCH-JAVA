package com.company.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.company.search.model.SearchDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SearchElasticsearchRepository {

    private static final Logger logger = LoggerFactory.getLogger(SearchElasticsearchRepository.class);

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${elasticsearch.index.name:search_documents}")
    private String indexName;

    public void createIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (!exists) {
                CreateIndexRequest request = CreateIndexRequest.of(i -> i
                        .index(indexName)
                        .mappings(m -> m
                                .properties("title", p -> p
                                        .text(t -> t
                                                .analyzer("standard")
                                                .fields("keyword", f -> f.keyword(k -> k.ignoreAbove(256)))
                                        )
                                )
                                .properties("content", p -> p
                                        .text(t -> t.analyzer("standard"))
                                )
                                .properties("category", p -> p
                                        .keyword(k -> k.ignoreAbove(256))
                                )
                                .properties("author", p -> p
                                        .text(t -> t
                                                .fields("keyword", f -> f.keyword(k -> k.ignoreAbove(256)))
                                        )
                                )
                                .properties("createdAt", p -> p
                                        .date(d -> d.format("strict_date_optional_time||epoch_millis"))
                                )
                                .properties("updatedAt", p -> p
                                        .date(d -> d.format("strict_date_optional_time||epoch_millis"))
                                )
                        )
                        .settings(s -> s
                                .numberOfShards("1")
                                .numberOfReplicas("0")
                        )
                );

                elasticsearchClient.indices().create(request);
                logger.info("Index '{}' created successfully", indexName);
            } else {
                logger.info("Index '{}' already exists", indexName);
            }
        } catch (IOException e) {
            logger.error("Error creating index: {}", e.getMessage());
            throw new RuntimeException("Failed to create index", e);
        }
    }

    public void deleteIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (exists) {
                DeleteIndexRequest request = DeleteIndexRequest.of(d -> d.index(indexName));
                elasticsearchClient.indices().delete(request);
                logger.info("Index '{}' deleted successfully", indexName);
            } else {
                logger.info("Index '{}' does not exist", indexName);
            }
        } catch (IOException e) {
            logger.error("Error deleting index: {}", e.getMessage());
            throw new RuntimeException("Failed to delete index", e);
        }
    }

    public void indexDocument(SearchDocument document) {
        try {
            logger.debug("Indexing document {} with createdAt: {}, updatedAt: {}", 
                      document.getId(), document.getCreatedAt(), document.getUpdatedAt());
            
            IndexRequest<SearchDocument> request = IndexRequest.of(i -> i
                    .index(indexName)
                    .id(document.getId())
                    .document(document)
            );

            IndexResponse response = elasticsearchClient.index(request);

            if (response.result() == Result.Created || response.result() == Result.Updated) {
                logger.debug("Document {} indexed successfully", document.getId());
            } else {
                logger.warn("Unexpected result when indexing document {}: {}",
                        document.getId(), response.result());
            }
        } catch (IOException e) {
            logger.error("Error indexing document {}: {}", document.getId(), e.getMessage());
            throw new RuntimeException("Failed to index document", e);
        }
    }

    public Optional<SearchDocument> getDocumentById(String id) {
        try {
            GetRequest request = GetRequest.of(g -> g
                    .index(indexName)
                    .id(id)
            );

            GetResponse<SearchDocument> response = elasticsearchClient.get(request, SearchDocument.class);

            if (response.found()) {
                return Optional.ofNullable(response.source());
            }
            return Optional.empty();
        } catch (IOException e) {
            logger.error("Error getting document {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get document", e);
        }
    }

    public void deleteDocument(String id) {
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(indexName)
                    .id(id)
            );

            DeleteResponse response = elasticsearchClient.delete(request);

            if (response.result() == Result.Deleted) {
                logger.debug("Document {} deleted successfully", id);
            } else if (response.result() == Result.NotFound) {
                logger.warn("Document {} not found for deletion", id);
            }
        } catch (IOException e) {
            logger.error("Error deleting document {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete document", e);
        }
    }

    public List<SearchDocument> search(String query, int size) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(q -> q
                            .multiMatch(m -> m
                                    .query(query)
                                    .fields("title^2", "content", "author")
                                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                            )
                    )
                    .size(size)
            );

            SearchResponse<SearchDocument> response = elasticsearchClient.search(request, SearchDocument.class);

            List<SearchDocument> results = new ArrayList<>();
            for (Hit<SearchDocument> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }

            logger.debug("Search query '{}' returned {} results", query, results.size());
            return results;

        } catch (IOException e) {
            logger.error("Error searching documents: {}", e.getMessage());
            throw new RuntimeException("Failed to search documents", e);
        }
    }

    public List<SearchDocument> searchByCategory(String category, int size) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(q -> q
                            .term(t -> t
                                    .field("category")
                                    .value(category)
                            )
                    )
                    .size(size)
            );

            SearchResponse<SearchDocument> response = elasticsearchClient.search(request, SearchDocument.class);

            List<SearchDocument> results = new ArrayList<>();
            for (Hit<SearchDocument> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }

            logger.debug("Category search '{}' returned {} results", category, results.size());
            return results;

        } catch (IOException e) {
            logger.error("Error searching by category: {}", e.getMessage());
            throw new RuntimeException("Failed to search by category", e);
        }
    }

    public List<SearchDocument> advancedSearch(Map<String, String> criteria, int size) {
        try {
            SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                    .index(indexName)
                    .size(size);

            // Construction de la requête bool
            List<co.elastic.clients.elasticsearch._types.query_dsl.Query> mustQueries = new ArrayList<>();

            if (criteria.containsKey("title")) {
                mustQueries.add(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q
                        .match(m -> m
                                .field("title")
                                .query(criteria.get("title"))
                        )
                ));
            }

            if (criteria.containsKey("content")) {
                mustQueries.add(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q
                        .match(m -> m
                                .field("content")
                                .query(criteria.get("content"))
                        )
                ));
            }

            if (criteria.containsKey("category")) {
                mustQueries.add(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q
                        .term(t -> t
                                .field("category")
                                .value(criteria.get("category"))
                        )
                ));
            }

            if (criteria.containsKey("author")) {
                mustQueries.add(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q
                        .match(m -> m
                                .field("author")
                                .query(criteria.get("author"))
                        )
                ));
            }

            if (!mustQueries.isEmpty()) {
                requestBuilder.query(q -> q
                        .bool(b -> b.must(mustQueries))
                );
            }

            SearchResponse<SearchDocument> response = elasticsearchClient.search(
                    requestBuilder.build(),
                    SearchDocument.class
            );

            List<SearchDocument> results = new ArrayList<>();
            for (Hit<SearchDocument> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }

            logger.debug("Advanced search returned {} results", results.size());
            return results;

        } catch (IOException e) {
            logger.error("Error in advanced search: {}", e.getMessage());
            throw new RuntimeException("Failed to perform advanced search", e);
        }
    }

    /**
     * Creates an alias from the hardcoded index name to the configured index name.
     * This ensures that the search repository can continue using the hardcoded name
     * while the actual data is stored in the configured index.
     */
    public void createIndexAlias() {
        try {
            // First, ensure the configured index exists
            createIndex();
            
            // Check if the alias already exists
            boolean aliasExists = elasticsearchClient.indices()
                .existsAlias(e -> e.name("documents"))
                .value();
                
            if (!aliasExists) {
                // Create the alias from "documents" to the configured index name
                elasticsearchClient.indices().putAlias(a -> a
                    .index(indexName)
                    .name("documents")
                );
                logger.info("Created alias 'documents' pointing to index '{}'", indexName);
            } else {
                logger.info("Alias 'documents' already exists");
            }
        } catch (IOException e) {
            logger.error("Error creating index alias: {}", e.getMessage());
            throw new RuntimeException("Failed to create index alias", e);
        }
    }
}
