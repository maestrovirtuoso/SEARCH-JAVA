package com.company.search.controller;

import com.company.search.model.dto.SearchRequest;
import com.company.search.model.dto.SearchResponse;
import com.company.search.model.dto.SearchResult;
import com.company.search.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/search")


public class SearchController {
    
    private final SearchService searchService;
    
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<SearchResponse<SearchResult>>> search(
            @Valid @RequestBody SearchRequest searchRequest) {
        return searchService.search(searchRequest)
            .thenApply(response -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(response));
    }
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<SearchResponse<SearchResult>>> searchSimple(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return searchService.searchSimple(query, page, size)
            .thenApply(response -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(response));
    }
    
    @GetMapping(value = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<SearchResponse<SearchResult>>> searchInFields(
            @RequestParam String query,
            @RequestParam List<String> fields,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return searchService.searchInFields(query, fields, page, size)
            .thenApply(response -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(response));
    }
    
    /**
     * Recherche des documents ayant un contenu similaire au texte fourni.
     *
     * @param text Le texte pour lequel trouver des documents similaires
     * @param page Page de résultats à récupérer (commence à 0)
     * @param size Nombre de résultats par page
     * @return Une réponse contenant les documents similaires
     */
    @PostMapping(value = "/similar-content", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<SearchResponse<SearchResult>>> searchSimilarContent(
            @RequestBody String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return searchService.searchSimilarContent(text, page, size)
            .thenApply(response -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(response));
    }
    
    /**
     * Endpoint pour les recherches avancées utilisant le langage de requête DSL d'Elasticsearch.
     * Permet de construire des requêtes structurées complexes avec des filtres, agrégations, etc.
     *
     * @param searchRequest La requête de recherche avancée
     * @return Une réponse contenant les documents correspondants
     */
    @PostMapping(value = "/advanced", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<SearchResponse<SearchResult>>> advancedSearch(
            @Valid @RequestBody SearchRequest searchRequest) {
        
        return searchService.advancedSearch(searchRequest)
            .thenApply(response -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(response));
    }
    
    /**
     * Endpoint pour la recherche en texte intégral qui analyse le texte et trouve des correspondances
     * intelligentes en utilisant des techniques comme la recherche floue, les synonymes, etc.
     *
     * @param query La requête de texte
     * @param fields Les champs dans lesquels chercher
     * @param matchType Le type de correspondance ("match", "match_phrase", "multi_match")
     * @param page La page à récupérer
     * @param size Le nombre d'éléments par page
     * @return Une réponse contenant les documents correspondants
     */
    @GetMapping(value = "/full-text", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<SearchResponse<SearchResult>>> fullTextSearch(
            @RequestParam String query,
            @RequestParam List<String> fields,
            @RequestParam(defaultValue = "multi_match") String matchType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return searchService.fullTextSearch(query, fields, matchType, page, size)
            .thenApply(response -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(response));
    }
    
    /**
     * Endpoint pour la recherche par terme qui trouve des correspondances exactes sans analyse.
     * Idéal pour les identifiants, les codes, ou les champs de type keyword.
     *
     * @param field Le champ sur lequel effectuer la recherche
     * @param value La valeur exacte à rechercher
     * @param type Le type de recherche par terme ("term", "prefix", "wildcard", "exists")
     * @param page La page à récupérer
     * @param size Le nombre d'éléments par page
     * @return Une réponse contenant les documents correspondants
     */
    @GetMapping(value = "/term", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<SearchResponse<SearchResult>>> termLevelSearch(
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(defaultValue = "term") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return searchService.termLevelSearch(field, value, type, page, size)
            .thenApply(response -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(response));
    }
}
