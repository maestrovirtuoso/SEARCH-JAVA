package com.company.search.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.InetSocketAddress;

@Configuration
public class ScyllaConfig {

    private static final Logger logger = LoggerFactory.getLogger(ScyllaConfig.class);

    @Value("${scylla.contact-points:localhost}")
    private String contactPoints;

    @Value("${scylla.port:9043}")
    private int port;

    @Value("${scylla.keyspace:search_data_dev}")
    private String keyspace;

    @Value("${scylla.datacenter:datacenter1}")
    private String datacenter;

    @Value("${scylla.replication-factor:1}")
    private int replicationFactor;

    @Bean
    public CqlSession cqlSession() {
        logger.info("Initializing ScyllaDB connection...");
        logger.info("Contact points: {}:{}", contactPoints, port);
        logger.info("Datacenter: {}", datacenter);
        logger.info("Target keyspace: {}", keyspace);

        // Étape 1 : Se connecter sans spécifier de keyspace
        CqlSession initialSession = null;
        try {
            initialSession = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress(contactPoints, port))
                    .withLocalDatacenter(datacenter)
                    .build();

            logger.info("Successfully connected to ScyllaDB");

            // Étape 2 : Créer le keyspace s'il n'existe pas
            createKeyspaceIfNotExists(initialSession);

            // Fermer la session initiale
            initialSession.close();
            logger.info("Initial session closed");

            // Étape 3 : Se reconnecter avec le keyspace
            logger.info("Connecting to ScyllaDB with keyspace: {}", keyspace);
            CqlSession keyspaceSession = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress(contactPoints, port))
                    .withLocalDatacenter(datacenter)
                    .withKeyspace(keyspace)
                    .build();

            logger.info("Successfully connected to keyspace: {}", keyspace);
            return keyspaceSession;

        } catch (Exception e) {
            logger.error("Failed to initialize ScyllaDB connection", e);
            if (initialSession != null && !initialSession.isClosed()) {
                try {
                    initialSession.close();
                } catch (Exception closeException) {
                    logger.warn("Error closing initial session", closeException);
                }
            }
            throw new RuntimeException("Failed to initialize ScyllaDB connection: " + e.getMessage(), e);
        }
    }

    private void createKeyspaceIfNotExists(CqlSession session) {
        String createKeyspaceQuery = String.format(
                "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = " +
                        "{'class': 'SimpleStrategy', 'replication_factor': %d}",
                keyspace, replicationFactor
        );

        logger.info("Creating keyspace if not exists: {}", keyspace);
        logger.debug("Keyspace creation query: {}", createKeyspaceQuery);

        try {
            session.execute(SimpleStatement.newInstance(createKeyspaceQuery));
            logger.info("Keyspace '{}' is ready", keyspace);
        } catch (Exception e) {
            logger.error("Error creating keyspace '{}': {}", keyspace, e.getMessage(), e);
            throw new RuntimeException("Failed to create keyspace: " + keyspace, e);
        }
    }
}