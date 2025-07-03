package com.company.search.util;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Utilitaire pour construire des requêtes de recherche structurées.
 * Principalement utilisé pour générer des requêtes compatibles avec des moteurs
 * de recherche comme Elasticsearch ou Solr.
 */
public class QueryBuilder {
    
    /**
     * Construit une requête de recherche multi-champs.
     * 
     * @param query Le terme ou phrase à rechercher
     * @param fields La liste des champs dans lesquels effectuer la recherche
     * @return Une requête au format "field1:(query) OR field2:(query) OR ..."
     *         ou simplement "query" si aucun champ n'est spécifié
     */
    public static String buildMultiFieldQuery(String query, List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return query; // Retourne la requête de base si aucun champ spécifié
        }
        
        // Combine les champs avec OR pour chercher dans plusieurs champs
        StringJoiner joiner = new StringJoiner(" OR ");
        for (String field : fields) {
            joiner.add(field + ":(" + query + ")"); // Format: nom_champ:(terme)
        }
        
        return joiner.toString();
    }
    
    /**
     * Construit une requête avec filtres appliqués.
     * 
     * @param query La requête principale
     * @param filters Map des filtres à appliquer (clé = champ, valeur = critère)
     * @return Une requête au format "(query) AND filtre1:valeur1 AND filtre2:valeur2..."
     */
    public static String buildFilteredQuery(String query, Map<String, Object> filters) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(query).append(")"); // Encapsule la requête principale
        
        if (filters != null && !filters.isEmpty()) {
            for (Map.Entry<String, Object> filter : filters.entrySet()) {
                builder.append(" AND ")
                       .append(filter.getKey()) // Nom du champ
                       .append(":")
                       .append(filter.getValue()); // Valeur exacte du filtre
            }
        }
        
        return builder.toString();
    }
    
    /**
     * Construit une requête booléenne complexe avec clauses MUST, SHOULD et MUST_NOT.
     * 
     * @param mustQuery Clause obligatoire (ET logique)
     * @param shouldQueries Clauses optionnelles (OU logique entre elles)
     * @param mustNotQueries Clauses d'exclusion (NOT logique)
     * @return Une requête combinant les différentes clauses
     */
    public static String buildBooleanQuery(String mustQuery, List<String> shouldQueries, List<String> mustNotQueries) {
        StringBuilder builder = new StringBuilder();
        
        // Clause MUST (obligatoire)
        if (mustQuery != null && !mustQuery.isEmpty()) {
            builder.append("(").append(mustQuery).append(")");
        }
        
        // Clauses SHOULD (au moins une doit matcher)
        if (shouldQueries != null && !shouldQueries.isEmpty()) {
            builder.append(" AND (");
            StringJoiner shouldJoiner = new StringJoiner(" OR "); // Combinaison en OU
            shouldQueries.forEach(shouldJoiner::add);
            builder.append(shouldJoiner.toString()).append(")");
        }
        
        // Clauses MUST_NOT (exclusion)
        if (mustNotQueries != null && !mustNotQueries.isEmpty()) {
            for (String mustNotQuery : mustNotQueries) {
                builder.append(" NOT (").append(mustNotQuery).append(")");
            }
        }
        
        return builder.toString();
    }
}