package com.company.search.service;

import com.company.search.model.dto.SearchRequest;
import com.company.search.model.dto.SearchResponse;
import com.company.search.model.dto.SearchResult;
import com.company.search.repository.SearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service principal pour les opérations de recherche.
 * Offre des fonctionnalités de recherche asynchrones avec suivi des performances.
 * 
 * <p>Ce service s'appuie sur SearchRepository pour interagir avec le moteur de recherche sous-jacent
 * (probablement Elasticsearch ou Solr). Toutes les opérations retournent des CompletableFuture
 * pour une exécution non-bloquante.</p>
 */
@Service
public class SearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    
    private final SearchRepository searchRepository;
    
    /**
     * Constructeur pour l'injection de dépendance.
     * 
     * @param searchRepository Le repository utilisé pour les opérations de recherche
     */
    public SearchService(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }
    
    /**
     * Méthode principale de recherche.
     * 
     * @param searchRequest Objet contenant tous les paramètres de recherche
     * @return Future contenant SearchResponse avec les résultats et métadonnées
     * 
     * <p>Exécute en parallèle :
     * 1. La recherche des résultats (avec filtres si spécifiés)
     * 2. Le comptage du nombre total de résultats
     * </p>
     */
    public CompletableFuture<SearchResponse<SearchResult>> search(SearchRequest searchRequest) {
        logger.info("Executing search query: {}", searchRequest.getQuery());
        
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<List<SearchResult>> resultsFuture;
        CompletableFuture<Long> countFuture;
        
        // Choix de la méthode de recherche en fonction de la présence de filtres
        if (searchRequest.getFilters() != null && !searchRequest.getFilters().isEmpty()) {
            resultsFuture = searchRepository.searchWithFilters(searchRequest);
        } else {
            resultsFuture = searchRepository.search(searchRequest);
        }
        
        countFuture = searchRepository.count(searchRequest.getQuery());
        
        // Combinaison des deux futures lorsque tous sont complétés
        return resultsFuture.thenCombine(countFuture, (results, totalHits) -> {
            long searchTime = System.currentTimeMillis() - startTime;
            
            SearchResponse<SearchResult> response = new SearchResponse<>(
                results, totalHits, searchRequest.getPage(), searchRequest.getSize()
            );
            response.setSearchTime(searchTime);
            
            logger.info("Search completed in {}ms, found {} results", searchTime, totalHits);
            return response;
        });
    }
    
    /**
     * Recherche simple avec pagination.
     * 
     * @param query Termes de recherche
     * @param page Numéro de page (commence à 0)
     * @param size Nombre de résultats par page
     */
    public CompletableFuture<SearchResponse<SearchResult>> searchSimple(String query, int page, int size) {
        SearchRequest request = new SearchRequest(query);
        request.setPage(page);
        request.setSize(size);
        return search(request);
    }
    
    /**
     * Recherche limitée à des champs spécifiques.
     * 
     * @param query Termes de recherche
     * @param fields Liste des champs où effectuer la recherche
     * @param page Numéro de page
     * @param size Taille de la page
     */
    public CompletableFuture<SearchResponse<SearchResult>> searchInFields(String query, List<String> fields, int page, int size) {
        SearchRequest request = new SearchRequest(query);
        request.setFields(fields);
        request.setPage(page);
        request.setSize(size);
        return search(request);
    }
    
    /**
     * Recherche par similarité sémantique.
     * 
     * @param text Texte de référence pour la similarité
     * @param page Numéro de page
     * @param size Taille de la page
     * @return Résultats classés par similarité au texte fourni
     * 
     * <p>Utilise probablement des algorithmes de type :
     * - BM25
     * - Similarité cosinus sur des embeddings
     * - Modèles de langue</p>
     */
    public CompletableFuture<SearchResponse<SearchResult>> searchSimilarContent(String text, int page, int size) {
        logger.info("Executing similar content search with text length: {}", text.length());
        
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<List<SearchResult>> resultsFuture = searchRepository.searchSimilarContent(text, page, size);
        CompletableFuture<Long> countFuture = searchRepository.countSimilarContent(text);
        
        return resultsFuture.thenCombine(countFuture, (results, totalHits) -> {
            long searchTime = System.currentTimeMillis() - startTime;
            
            SearchResponse<SearchResult> response = new SearchResponse<>(
                results, totalHits, page, size
            );
            response.setSearchTime(searchTime);
            
            logger.info("Similar content search completed in {}ms, found {} results", searchTime, totalHits);
            return response;
        });
    }
    
    /**
     * Recherche utilisant la syntaxe DSL native du moteur de recherche.
     * 
     * @param searchRequest Requête complexe avec paramètres avancés
     * @return Résultats correspondant à la requête DSL
     * 
     * <p>Permet d'utiliser toutes les fonctionnalités avancées :
     * - Recherches géospatiales
     * - Agrégations
     * - Scripts personnalisés</p>
     */
    public CompletableFuture<SearchResponse<SearchResult>> advancedSearch(SearchRequest searchRequest) {
        logger.info("Executing advanced search with DSL query");
        
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<List<SearchResult>> resultsFuture = searchRepository.search(searchRequest);
        CompletableFuture<Long> countFuture = searchRepository.count(searchRequest.getQuery());
        
        return resultsFuture.thenCombine(countFuture, (results, totalHits) -> {
            long searchTime = System.currentTimeMillis() - startTime;
            
            SearchResponse<SearchResult> response = new SearchResponse<>(
                results, totalHits, searchRequest.getPage(), searchRequest.getSize()
            );
            response.setSearchTime(searchTime);
            
            logger.info("Advanced search completed in {}ms, found {} results", searchTime, totalHits);
            return response;
        });
    }
    
    /**
     * Recherche en texte intégral avec analyse linguistique.
     * 
     * @param query Texte à rechercher
     * @param fields Champs cibles
     * @param matchType Type de correspondance :
     *        "match" (standard), "match_phrase" (expression exacte), 
     *        "multi_match" (multi-champs)
     * @param page Numéro de page
     * @param size Taille de la page
     * @return Résultats pertinents selon l'analyse textuelle
     * 
     * <p>Fonctionnalités incluses :
     * - Stemming
     * - Synonymes
     * - Tolérance aux fautes (fuzziness)
     * - Analyse sémantique</p>
     */
    public CompletableFuture<SearchResponse<SearchResult>> fullTextSearch(
            String query, List<String> fields, String matchType, int page, int size) {
        
        logger.info("Executing full-text search: '{}' in fields: {}, type: {}", 
                   query, fields, matchType);
        
        long startTime = System.currentTimeMillis();
        
        String fuzziness = "AUTO"; // Tolérance automatique aux fautes de frappe
        
        CompletableFuture<List<SearchResult>> resultsFuture = 
            searchRepository.fullTextSearch(query, fields, matchType, page, size, fuzziness);
        CompletableFuture<Long> countFuture = 
            searchRepository.countFullTextSearch(query, fields, matchType, fuzziness);
        
        return resultsFuture.thenCombine(countFuture, (results, totalHits) -> {
            long searchTime = System.currentTimeMillis() - startTime;
            
            SearchResponse<SearchResult> response = new SearchResponse<>(
                results, totalHits, page, size
            );
            response.setSearchTime(searchTime);
            
            logger.info("Full-text search completed in {}ms, found {} results", searchTime, totalHits);
            return response;
        });
    }
    
    /**
     * Recherche exacte sans analyse textuelle.
     * 
     * @param field Champ à filtrer
     * @param value Valeur exacte à trouver
     * @param type Type de recherche :
     *        "term" (exact), "prefix" (préfixe), 
     *        "wildcard" (avec jokers), "exists" (présence)
     * @param page Numéro de page
     * @param size Taille de la page
     * @return Résultats correspondant au filtre exact
     * 
     * <p>Typiquement utilisé pour :
     * - Filtres booléens
     * - Catégories
     * - Tags
     * - Champs non analysés</p>
     */
    public CompletableFuture<SearchResponse<SearchResult>> termLevelSearch(
            String field, String value, String type, int page, int size) {
        
        logger.info("Executing term-level search: field '{}', value '{}', type: {}", 
                   field, value, type);
        
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<List<SearchResult>> resultsFuture = 
            searchRepository.termLevelSearch(field, value, type, page, size);
        CompletableFuture<Long> countFuture = 
            searchRepository.countTermLevelSearch(field, value, type);
        
        return resultsFuture.thenCombine(countFuture, (results, totalHits) -> {
            long searchTime = System.currentTimeMillis() - startTime;
            
            SearchResponse<SearchResult> response = new SearchResponse<>(
                results, totalHits, page, size
            );
            response.setSearchTime(searchTime);
            
            logger.info("Term-level search completed in {}ms, found {} results", searchTime, totalHits);
            return response;
        });
    }
}