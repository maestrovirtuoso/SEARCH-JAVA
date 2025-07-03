package com.company.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.search.model.SearchDocument;
import com.company.search.model.dto.SearchRequest;
import com.company.search.model.dto.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Repository
public class SearchRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchRepository.class);
    private final ElasticsearchClient client;
    
    public SearchRepository(ElasticsearchClient client) {
        this.client = client;
    }
    
    public CompletableFuture<List<SearchResult>> search(SearchRequest searchRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int from = searchRequest.getPage() * searchRequest.getSize();
                
                co.elastic.clients.elasticsearch.core.SearchRequest.Builder requestBuilder = 
                    new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                        .index("documents")
                        .from(from)
                        .size(searchRequest.getSize());
                
                // Construction de la requête
                if (searchRequest.getFields() != null && !searchRequest.getFields().isEmpty()) {
                    requestBuilder.query(q -> q
                        .multiMatch(m -> m
                            .query(searchRequest.getQuery())
                            .fields(searchRequest.getFields())
                        )
                    );
                } else {
                    requestBuilder.query(q -> q
                        .queryString(qs -> qs
                            .query(searchRequest.getQuery())
                        )
                    );
                }
                
                // Ajout du tri si spécifié
                if (searchRequest.getSortBy() != null) {
                    requestBuilder.sort(s -> s
                        .field(f -> f
                            .field(searchRequest.getSortBy())
                            .order("asc".equalsIgnoreCase(searchRequest.getSortOrder()) ? 
                                co.elastic.clients.elasticsearch._types.SortOrder.Asc : 
                                co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                        )
                    );
                }
                
                // Ajout du highlighting
                requestBuilder.highlight(h -> h
                    .fields("title", hf -> hf)
                    .fields("content", hf -> hf)
                    .preTags("<strong>")
                    .postTags("</strong>")
                );
                
                co.elastic.clients.elasticsearch.core.SearchResponse<SearchDocument> response = 
                    client.search(requestBuilder.build(), SearchDocument.class);
                
                return response.hits().hits().stream()
                    .map(this::mapHitToSearchResult)
                    .collect(Collectors.toList());
                    
            } catch (IOException e) {
                throw new RuntimeException("Failed to search documents", e);
            }
        });
    }
    
    public CompletableFuture<Long> count(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CountRequest request = CountRequest.of(c -> c
                    .index("documents")
                    .query(q -> q
                        .queryString(qs -> qs.query(query))
                    )
                );
                
                CountResponse response = client.count(request);
                return response.count();
            } catch (IOException e) {
                throw new RuntimeException("Failed to count documents", e);
            }
        });
    }
    
    public CompletableFuture<List<SearchResult>> searchWithFilters(SearchRequest searchRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int from = searchRequest.getPage() * searchRequest.getSize();
                
                co.elastic.clients.elasticsearch.core.SearchRequest.Builder requestBuilder = 
                    new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                        .index("documents")
                        .from(from)
                        .size(searchRequest.getSize());
                
                // Construction de la requête avec filtres
                requestBuilder.query(q -> q
                    .bool(b -> {
                        // Requête principale
                        b.must(m -> m
                            .queryString(qs -> qs.query(searchRequest.getQuery()))
                        );
                        
                        // Ajout des filtres
                        if (searchRequest.getFilters() != null) {
                            searchRequest.getFilters().forEach((field, value) -> {
                                b.filter(f -> f
                                    .term(t -> t.field(field).value(value.toString()))
                                );
                            });
                        }
                        
                        return b;
                    })
                );
                
                co.elastic.clients.elasticsearch.core.SearchResponse<SearchDocument> response = 
                    client.search(requestBuilder.build(), SearchDocument.class);
                
                return response.hits().hits().stream()
                    .map(this::mapHitToSearchResult)
                    .collect(Collectors.toList());
                    
            } catch (IOException e) {
                throw new RuntimeException("Failed to search documents with filters", e);
            }
        });
    }

    /**
     * Recherche des documents ayant un contenu similaire à celui du texte fourni
     * en utilisant un "more like this query" d'Elasticsearch.
     *
     * @param text Le texte pour lequel trouver des documents similaires
     * @param page Page de résultats à récupérer
     * @param size Nombre de résultats par page
     * @return Une liste de documents similaires
     */
    public CompletableFuture<List<SearchResult>> searchSimilarContent(String text, int page, int size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int from = page * size;
                
                co.elastic.clients.elasticsearch.core.SearchRequest.Builder requestBuilder = 
                    new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                        .index("documents")
                        .from(from)
                        .size(size)
                        .query(q -> q
                            .moreLikeThis(m -> m
                                .fields("content")
                                .like(l -> l.text(text))
                                .minTermFreq(1)
                                .maxQueryTerms(12)
                                .minDocFreq(1)
                            )
                        )
                        .highlight(h -> h
                            .fields("content", hf -> hf)
                            .preTags("<strong>")
                            .postTags("</strong>")
                            .fragmentSize(150)
                            .numberOfFragments(3)
                        );
                
                co.elastic.clients.elasticsearch.core.SearchResponse<SearchDocument> response = 
                    client.search(requestBuilder.build(), SearchDocument.class);
                
                return response.hits().hits().stream()
                    .map(this::mapHitToSearchResult)
                    .collect(Collectors.toList());
                    
            } catch (IOException e) {
                logger.error("Failed to search similar content: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to search similar content", e);
            }
        });
    }
    
    /**
     * Compte le nombre de documents ayant un contenu similaire à celui du texte fourni.
     *
     * @param text Le texte pour lequel compter les documents similaires
     * @return Le nombre de documents similaires
     */
    public CompletableFuture<Long> countSimilarContent(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CountRequest request = CountRequest.of(c -> c
                    .index("documents")
                    .query(q -> q
                        .moreLikeThis(m -> m
                            .fields("content")
                            .like(l -> l.text(text))
                            .minTermFreq(1)
                            .maxQueryTerms(12)
                            .minDocFreq(1)
                        )
                    )
                );
                
                CountResponse response = client.count(request);
                return response.count();
            } catch (IOException e) {
                logger.error("Failed to count similar content: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to count similar content", e);
            }
        });
    }
    
    /**
     * Effectue une recherche avancée en utilisant le DSL Elasticsearch (Domain Specific Language).
     * Permet de construire des requêtes structurées complexes.
     *
     * @param query La requête JSON DSL Elasticsearch sous forme de Map
     * @param page La page à récupérer
     * @param size Le nombre d'éléments par page
     * @return Une liste de résultats de recherche
     */
    public CompletableFuture<List<SearchResult>> searchWithDSL(Map<String, Object> query, int page, int size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int from = page * size;
                
                // Conversion de la Map en JSON pour Elasticsearch
                String queryJson = new ObjectMapper().writeValueAsString(query);
                
                // Création de la requête de recherche
                co.elastic.clients.elasticsearch.core.SearchRequest searchRequest = 
                    co.elastic.clients.elasticsearch.core.SearchRequest.of(s -> s
                        .index("documents")
                        .from(from)
                        .size(size)
                        .withJson(new StringReader(queryJson))
                    );
                
                co.elastic.clients.elasticsearch.core.SearchResponse<SearchDocument> response = 
                    client.search(searchRequest, SearchDocument.class);
                
                return response.hits().hits().stream()
                    .map(this::mapHitToSearchResult)
                    .collect(Collectors.toList());
                    
            } catch (IOException e) {
                logger.error("Failed to execute DSL search: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to execute DSL search", e);
            }
        });
    }
    
    /**
     * Effectue une recherche en texte intégral sur les champs spécifiés.
     * Utilise les analyseurs Elasticsearch pour une recherche intelligente.
     * 
     * @param text Le texte à rechercher
     * @param fields Les champs dans lesquels rechercher
     * @param matchType Le type de correspondance ("match", "match_phrase", "multi_match")
     * @param page La page à récupérer
     * @param size Le nombre d'éléments par page
     * @param fuzziness Niveau de tolérance aux fautes de frappe (0, 1, 2 ou "AUTO")
     * @return Une liste de résultats de recherche
     */
    public CompletableFuture<List<SearchResult>> fullTextSearch(String text, List<String> fields, 
                                                             String matchType, int page, int size, 
                                                             String fuzziness) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int from = page * size;
                
                co.elastic.clients.elasticsearch.core.SearchRequest.Builder requestBuilder = 
                    new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                        .index("documents")
                        .from(from)
                        .size(size);
                
                // Construction de la requête selon le type de correspondance
                switch(matchType.toLowerCase()) {
                    case "match":
                        requestBuilder.query(q -> q
                            .match(m -> m
                                .field(fields.get(0))
                                .query(text)
                                .fuzziness(fuzziness)
                            )
                        );
                        break;
                    case "match_phrase":
                        requestBuilder.query(q -> q
                            .matchPhrase(m -> m
                                .field(fields.get(0))
                                .query(text)
                            )
                        );
                        break;
                    case "multi_match":
                    default:
                        requestBuilder.query(q -> q
                            .multiMatch(m -> m
                                .fields(fields)
                                .query(text)
                                .fuzziness(fuzziness)
                                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                            )
                        );
                        break;
                }
                
                // Ajout du highlighting
                requestBuilder.highlight(h -> {
                    h.preTags("<strong>");
                    h.postTags("</strong>");
                    h.fragmentSize(150);
                    h.numberOfFragments(3);
                    
                    for (String field : fields) {
                        h.fields(field, hf -> hf);
                    }
                    return h;
                });
                
                co.elastic.clients.elasticsearch.core.SearchResponse<SearchDocument> response = 
                    client.search(requestBuilder.build(), SearchDocument.class);
                
                return response.hits().hits().stream()
                    .map(this::mapHitToSearchResult)
                    .collect(Collectors.toList());
                    
            } catch (IOException e) {
                logger.error("Failed to execute full text search: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to execute full text search", e);
            }
        });
    }
    
    /**
     * Effectue une recherche par terme (sans analyse) sur des valeurs exactes.
     * Idéal pour les identifiants, les codes, ou les champs de type keyword.
     * 
     * @param field Le champ sur lequel effectuer la recherche
     * @param value La valeur exacte à rechercher
     * @param type Le type de recherche par terme ("term", "terms", "prefix", "wildcard", "exists")
     * @param page La page à récupérer
     * @param size Le nombre d'éléments par page
     * @return Une liste de résultats de recherche
     */
    public CompletableFuture<List<SearchResult>> termLevelSearch(String field, Object value, 
                                                              String type, int page, int size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int from = page * size;
                
                co.elastic.clients.elasticsearch.core.SearchRequest.Builder requestBuilder = 
                    new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                        .index("documents")
                        .from(from)
                        .size(size);
                
                // Construction de la requête selon le type de recherche par terme
                switch(type.toLowerCase()) {
                    case "term":
                        requestBuilder.query(q -> q
                            .term(t -> t
                                .field(field)
                                .value(value.toString())
                            )
                        );
                        break;
                    case "terms":
                        if (value instanceof List) {
                            List<?> values = (List<?>) value;
                            List<String> stringValues = values.stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                            
                            // Create a list of term queries, one for each value
                            List<Query> termQueries = stringValues.stream()
                                .map(val -> Query.of(tq -> tq
                                    .term(t -> t
                                        .field(field)
                                        .value(val)
                                    )
                                ))
                                .collect(Collectors.toList());
                            
                            // Add the terms query as a bool should query
                            requestBuilder.query(q -> q
                                .bool(b -> b
                                    .should(termQueries)
                                )
                            );
                        }
                        break;
                    case "prefix":
                        requestBuilder.query(q -> q
                            .prefix(p -> p
                                .field(field)
                                .value(value.toString())
                            )
                        );
                        break;
                    case "wildcard":
                        requestBuilder.query(q -> q
                            .wildcard(w -> w
                                .field(field)
                                .value(value.toString())
                            )
                        );
                        break;
                    case "exists":
                        requestBuilder.query(q -> q
                            .exists(e -> e
                                .field(field)
                            )
                        );
                        break;
                    default:
                        throw new IllegalArgumentException("Type de recherche par terme non supporté: " + type);
                }
                
                co.elastic.clients.elasticsearch.core.SearchResponse<SearchDocument> response = 
                    client.search(requestBuilder.build(), SearchDocument.class);
                
                return response.hits().hits().stream()
                    .map(this::mapHitToSearchResult)
                    .collect(Collectors.toList());
                    
            } catch (IOException e) {
                logger.error("Failed to execute term level search: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to execute term level search", e);
            }
        });
    }
    
    /**
     * Compte le nombre de résultats pour une recherche par DSL.
     * 
     * @param query La requête JSON DSL Elasticsearch sous forme de Map
     * @return Le nombre de documents correspondants
     */
    public CompletableFuture<Long> countWithDSL(Map<String, Object> query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Conversion de la Map en JSON pour Elasticsearch
                String queryJson = new ObjectMapper().writeValueAsString(query);
                
                // Création de la requête de comptage
                CountRequest countRequest = CountRequest.of(c -> c
                    .index("documents")
                    .withJson(new StringReader(queryJson))
                );
                
                CountResponse response = client.count(countRequest);
                return response.count();
            } catch (IOException e) {
                logger.error("Failed to count with DSL: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to count with DSL", e);
            }
        });
    }
    
    /**
     * Compte le nombre de résultats pour une recherche en texte intégral.
     */
    public CompletableFuture<Long> countFullTextSearch(String text, List<String> fields, String matchType, String fuzziness) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CountRequest.Builder requestBuilder = new CountRequest.Builder()
                    .index("documents");
                
                // Construction de la requête selon le type de correspondance
                switch(matchType.toLowerCase()) {
                    case "match":
                        requestBuilder.query(q -> q
                            .match(m -> m
                                .field(fields.get(0))
                                .query(text)
                                .fuzziness(fuzziness)
                            )
                        );
                        break;
                    case "match_phrase":
                        requestBuilder.query(q -> q
                            .matchPhrase(m -> m
                                .field(fields.get(0))
                                .query(text)
                            )
                        );
                        break;
                    case "multi_match":
                    default:
                        requestBuilder.query(q -> q
                            .multiMatch(m -> m
                                .fields(fields)
                                .query(text)
                                .fuzziness(fuzziness)
                            )
                        );
                        break;
                }
                
                CountResponse response = client.count(requestBuilder.build());
                return response.count();
            } catch (IOException e) {
                logger.error("Failed to count full text search: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to count full text search", e);
            }
        });
    }
    
    /**
     * Compte le nombre de résultats pour une recherche par terme.
     */
    public CompletableFuture<Long> countTermLevelSearch(String field, Object value, String type) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CountRequest.Builder requestBuilder = new CountRequest.Builder()
                    .index("documents");
                
                // Construction de la requête selon le type de recherche par terme
                switch(type.toLowerCase()) {
                    case "term":
                        requestBuilder.query(q -> q
                            .term(t -> t
                                .field(field)
                                .value(value.toString())
                            )
                        );
                        break;
                    case "terms":
                        if (value instanceof List) {
                            List<?> values = (List<?>) value;
                            List<String> stringValues = values.stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                            
                            // Create a list of term queries, one for each value
                            List<Query> termQueries = stringValues.stream()
                                .map(val -> Query.of(tq -> tq
                                    .term(t -> t
                                        .field(field)
                                        .value(val)
                                    )
                                ))
                                .collect(Collectors.toList());
                            
                            // Add the terms query as a bool should query
                            requestBuilder.query(q -> q
                                .bool(b -> b
                                    .should(termQueries)
                                )
                            );
                        }
                        break;
                    case "prefix":
                        requestBuilder.query(q -> q
                            .prefix(p -> p
                                .field(field)
                                .value(value.toString())
                            )
                        );
                        break;
                    case "wildcard":
                        requestBuilder.query(q -> q
                            .wildcard(w -> w
                                .field(field)
                                .value(value.toString())
                            )
                        );
                        break;
                    case "exists":
                        requestBuilder.query(q -> q
                            .exists(e -> e
                                .field(field)
                            )
                        );
                        break;
                    default:
                        throw new IllegalArgumentException("Type de recherche par terme non supporté: " + type);
                }
                
                CountResponse response = client.count(requestBuilder.build());
                return response.count();
            } catch (IOException e) {
                logger.error("Failed to count term level search: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to count term level search", e);
            }
        });
    }
    
    private SearchResult mapHitToSearchResult(Hit<SearchDocument> hit) {
        // Handle null score safely
        Double scoreObj = hit.score();
        float score = scoreObj != null ? scoreObj.floatValue() : 0.0f;
        
        SearchResult result = new SearchResult(hit.source(), score);

        // Ajout du highlighting si disponible
        if (hit.highlight() != null && !hit.highlight().isEmpty()) {
            List<String> highlights = hit.highlight().values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            result.setHighlight(highlights.toArray(new String[0]));
        }

        return result;
    }
}