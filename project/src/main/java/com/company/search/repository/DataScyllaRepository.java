package com.company.search.repository;

import com.company.search.model.SearchDocument;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;


@Repository
@DependsOn("scyllaSchemaInitializer")
public class DataScyllaRepository {

    private static final Logger logger = LoggerFactory.getLogger(DataScyllaRepository.class);

    @Autowired
    private CqlSession cqlSession;

    @Autowired
    private ObjectMapper objectMapper;

    private PreparedStatement insertStatement;
    private PreparedStatement selectByIdStatement;
    private PreparedStatement selectAllStatement;
    private PreparedStatement selectByCategoryStatement;
    private PreparedStatement updateStatement;
    private PreparedStatement deleteStatement;

    @PostConstruct
    public void init() {
        logger.info("Initializing prepared statements for DataScyllaRepository");

        this.insertStatement = cqlSession.prepare(
                "INSERT INTO documents (id, title, content, category, author, metadata, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );

        this.selectByIdStatement = cqlSession.prepare(
                "SELECT * FROM documents WHERE id = ?"
        );

        this.selectAllStatement = cqlSession.prepare(
                "SELECT * FROM documents"
        );

        this.selectByCategoryStatement = cqlSession.prepare(
                "SELECT * FROM documents WHERE category = ? ALLOW FILTERING"
        );

        this.updateStatement = cqlSession.prepare(
                "UPDATE documents SET title = ?, content = ?, category = ?, author = ?, " +
                        "metadata = ?, updated_at = ? WHERE id = ?"
        );

        this.deleteStatement = cqlSession.prepare(
                "DELETE FROM documents WHERE id = ?"
        );

        logger.info("Prepared statements initialized successfully");
    }

    public SearchDocument save(SearchDocument document) {
        try {
            Map<String, String> metadataAsString = convertMetadataToStringMap(document.getMetadata());

            cqlSession.execute(insertStatement.bind(
                    document.getId(),
                    document.getTitle(),
                    document.getContent(),
                    document.getCategory(),
                    document.getAuthor(),
                    metadataAsString,
                    document.getCreatedAt(),
                    document.getUpdatedAt()
            ));
            logger.debug("Document saved successfully: {}", document.getId());
            return document; // AJOUTER cette ligne
        } catch (Exception e) {
            logger.error("Error saving document: {}", e.getMessage());
            throw new RuntimeException("Failed to save document", e);
        }
    }

    // 2. Ajoutez cette nouvelle méthode count
    public long count() {
        try {
            logger.debug("Counting documents");
            ResultSet result = cqlSession.execute("SELECT COUNT(*) FROM documents");
            Row row = result.one();
            long count = row != null ? row.getLong(0) : 0;
            logger.debug("Total documents count: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error counting documents: {}", e.getMessage());
            throw new RuntimeException("Failed to count documents", e);
        }
    }

    public Optional<SearchDocument> findById(String id) {
        try {
            ResultSet resultSet = cqlSession.execute(selectByIdStatement.bind(id));
            Row row = resultSet.one();

            if (row != null) {
                return Optional.of(mapRowToDocument(row));
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding document by id: {}", e.getMessage());
            throw new RuntimeException("Failed to find document", e);
        }
    }

    public List<SearchDocument> findAll() {
        List<SearchDocument> documents = new ArrayList<>();
        try {
            ResultSet resultSet = cqlSession.execute(selectAllStatement.bind());

            for (Row row : resultSet) {
                documents.add(mapRowToDocument(row));
            }
            return documents;
        } catch (Exception e) {
            logger.error("Error finding all documents: {}", e.getMessage());
            throw new RuntimeException("Failed to find documents", e);
        }
    }

    public List<SearchDocument> findByCategory(String category) {
        List<SearchDocument> documents = new ArrayList<>();
        try {
            ResultSet resultSet = cqlSession.execute(selectByCategoryStatement.bind(category));

            for (Row row : resultSet) {
                documents.add(mapRowToDocument(row));
            }
            return documents;
        } catch (Exception e) {
            logger.error("Error finding documents by category: {}", e.getMessage());
            throw new RuntimeException("Failed to find documents", e);
        }
    }

    public void update(SearchDocument document) {
        try {
            Map<String, String> metadataAsString = convertMetadataToStringMap(document.getMetadata());

            cqlSession.execute(updateStatement.bind(
                    document.getTitle(),
                    document.getContent(),
                    document.getCategory(),
                    document.getAuthor(),
                    metadataAsString,
                    document.getUpdatedAt(),
                    document.getId()
            ));
            logger.debug("Document updated successfully: {}", document.getId());
        } catch (Exception e) {
            logger.error("Error updating document: {}", e.getMessage());
            throw new RuntimeException("Failed to update document", e);
        }
    }

    public void deleteById(String id) {
        try {
            cqlSession.execute(deleteStatement.bind(id));
            logger.debug("Document deleted successfully: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting document: {}", e.getMessage());
            throw new RuntimeException("Failed to delete document", e);
        }
    }

    private SearchDocument mapRowToDocument(Row row) {
        SearchDocument document = new SearchDocument();
        document.setId(row.getString("id"));
        document.setTitle(row.getString("title"));
        document.setContent(row.getString("content"));
        document.setCategory(row.getString("category"));
        document.setAuthor(row.getString("author"));
        document.setCreatedAt(row.getInstant("created_at"));
        document.setUpdatedAt(row.getInstant("updated_at"));

        Map<String, String> metadataAsString = row.getMap("metadata", String.class, String.class);
        Map<String, Object> metadata = convertStringMapToMetadata(metadataAsString);
        document.setMetadata(metadata);

        return document;
    }

    private Map<String, String> convertMetadataToStringMap(Map<String, Object> metadata) {
        if (metadata == null) {
            return new HashMap<>();
        }

        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            try {
                String value = objectMapper.writeValueAsString(entry.getValue());
                stringMap.put(entry.getKey(), value);
            } catch (JsonProcessingException e) {
                logger.warn("Could not serialize metadata value for key {}: {}", entry.getKey(), e.getMessage());
                stringMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return stringMap;
    }

    private Map<String, Object> convertStringMapToMetadata(Map<String, String> stringMap) {
        if (stringMap == null) {
            return new HashMap<>();
        }

        Map<String, Object> metadata = new HashMap<>();
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            try {
                Object value = objectMapper.readValue(entry.getValue(), new TypeReference<Object>() {});
                metadata.put(entry.getKey(), value);
            } catch (JsonProcessingException e) {
                // Si la désérialisation échoue, garder la valeur comme String
                metadata.put(entry.getKey(), entry.getValue());
            }
        }
        return metadata;
    }
}


