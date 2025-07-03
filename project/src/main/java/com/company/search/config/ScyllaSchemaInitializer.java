package com.company.search.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ScyllaSchemaInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ScyllaSchemaInitializer.class);

    @Autowired
    private CqlSession cqlSession;

    @Value("${scylla.schema.auto-create:true}")
    private boolean autoCreateSchema;

    @PostConstruct
    public void initializeSchema() {
        if (!autoCreateSchema) {
            logger.info("Schema auto-creation is disabled");
            return;
        }

        logger.info("Initializing ScyllaDB schema...");

        try {
            createTables();
            createIndexes();
            logger.info("Schema initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error initializing schema: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize schema", e);
        }
    }

    private void createTables() {
        List<String> tableCreationQueries = Arrays.asList(
                // Table documents - adaptée pour SearchDocument
                "CREATE TABLE IF NOT EXISTS documents (" +
                "    id TEXT PRIMARY KEY," +
                "    title TEXT," +
                "    content TEXT," +
                "    category TEXT," +
                "    author TEXT," +
                "    tags LIST<TEXT>," +
                "    metadata MAP<TEXT, TEXT>," +
                "    created_at TIMESTAMP," +
                "    updated_at TIMESTAMP" +
                ")",

                // Table pour l'historique des recherches
                "CREATE TABLE IF NOT EXISTS search_history (" +
                "    user_id UUID," +
                "    search_query TEXT," +
                "    search_timestamp TIMESTAMP," +
                "    results_count INT," +
                "    PRIMARY KEY (user_id, search_timestamp)" +
                ") WITH CLUSTERING ORDER BY (search_timestamp DESC)",

                // Table pour les index inversés (si nécessaire)
                "CREATE TABLE IF NOT EXISTS inverted_index (" +
                "    term TEXT," +
                "    document_id TEXT," +
                "    frequency INT," +
                "    positions LIST<INT>," +
                "    PRIMARY KEY (term, document_id)" +
                ")",

                // Table pour les statistiques des documents
                "CREATE TABLE IF NOT EXISTS document_stats (" +
                "    category TEXT," +
                "    total_documents COUNTER," +
                "    PRIMARY KEY (category)" +
                ")"
        );

        for (String query : tableCreationQueries) {
            try {
                cqlSession.execute(SimpleStatement.newInstance(query));
                logger.debug("Table creation query executed successfully");
            } catch (Exception e) {
                logger.error("Error executing table creation query: {}", e.getMessage());
                throw e;
            }
        }
    }

    private void createIndexes() {
        List<String> indexCreationQueries = Arrays.asList(
                // Index sur la catégorie - CORRIGÉ: utilise la bonne table 'documents'
                "CREATE INDEX IF NOT EXISTS idx_category ON documents (category)",

                // Index sur l'auteur
                "CREATE INDEX IF NOT EXISTS idx_author ON documents (author)",

                // Index sur created_at pour les requêtes temporelles
                "CREATE INDEX IF NOT EXISTS idx_created_at ON documents (created_at)"

                // Note: Pour la recherche full-text sur le contenu, utilisez Elasticsearch
                // ScyllaDB ne supporte pas nativement les index SASI comme Cassandra
        );

        for (String query : indexCreationQueries) {
            try {
                cqlSession.execute(SimpleStatement.newInstance(query));
                logger.debug("Index creation query executed successfully: {}", query);
            } catch (Exception e) {
                // Les index peuvent déjà exister, on log juste un warning
                logger.warn("Could not create index (may already exist): {}", e.getMessage());
            }
        }
    }

    /**
     * Méthode utilitaire pour vérifier la connectivité
     */
    public boolean testConnection() {
        try {
            ResultSet result = cqlSession.execute("SELECT release_version FROM system.local");
            logger.info("ScyllaDB connection test successful");
            return true;
        } catch (Exception e) {
            logger.error("ScyllaDB connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Méthode pour nettoyer le schéma (utile pour les tests)
     */
    public void dropTables() {
        List<String> dropQueries = Arrays.asList(
                "DROP TABLE IF EXISTS documents",
                "DROP TABLE IF EXISTS search_history",
                "DROP TABLE IF EXISTS inverted_index",
                "DROP TABLE IF EXISTS document_stats"
        );

        for (String query : dropQueries) {
            try {
                cqlSession.execute(SimpleStatement.newInstance(query));
                logger.debug("Table dropped successfully");
            } catch (Exception e) {
                logger.warn("Could not drop table: {}", e.getMessage());
            }
        }
    }
}