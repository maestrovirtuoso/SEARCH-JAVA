package com.company.search.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour le traitement des requêtes de recherche.
 * Fournit des méthodes pour normaliser, valider et transformer les termes de recherche.
 */
public class SearchUtil {
    
    // Expression régulière pour détecter les caractères spéciaux dans les requêtes
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[\\[\\]{}()*+?.\\\\^$|]");
    
    /**
     * Échappe les caractères spéciaux dans une requête pour éviter 
     * qu'ils soient interprétés comme des opérateurs de recherche.
     * 
     * @param query La requête à échapper
     * @return La requête avec les caractères spéciaux échappés (préfixés par \)
     * 
     * Exemple : "java?" devient "java\?"
     */
    public static String escapeSpecialCharacters(String query) {
        return SPECIAL_CHARS.matcher(query).replaceAll("\\\\$0");
    }
    
    /**
     * Découpe une requête en tokens individuels (mots séparés par des espaces).
     * 
     * @param query La requête à tokenizer
     * @return Liste des tokens en minuscules
     * 
     * Exemple : "Hello World" → ["hello", "world"]
     */
    public static List<String> tokenizeQuery(String query) {
        return Arrays.asList(query.toLowerCase().split("\\s+"));
    }
    
    /**
     * Construit une requête avec wildcards pour une recherche partielle.
     * 
     * @param query Le terme à rechercher
     * @return La requête au format *terme* (insensible à la casse)
     * 
     * Exemple : "java" → "*java*"
     */
    public static String buildWildcardQuery(String query) {
        return "*" + query.toLowerCase() + "*";
    }
    
    /**
     * Vérifie si un terme de recherche est valide.
     * 
     * @param term Le terme à valider
     * @return true si le terme est non-null et contient au moins 2 caractères non-blancs
     */
    public static boolean isValidSearchTerm(String term) {
        return term != null && term.trim().length() >= 2;
    }
    
    /**
     * Normalise une requête pour la recherche :
     * - Supprime les espaces en début/fin
     * - Convertit en minuscules
     * - Retourne une chaîne vide si null
     * 
     * @param query La requête à normaliser
     * @return La requête normalisée
     */
    public static String normalizeQuery(String query) {
        if (query == null) return "";
        return query.trim().toLowerCase();
    }
}