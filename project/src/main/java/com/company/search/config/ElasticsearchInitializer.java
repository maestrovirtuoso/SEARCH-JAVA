package com.company.search.config;

import com.company.search.repository.SearchElasticsearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchInitializer.class);
    
    @Bean
    public ApplicationRunner initializeElasticsearch(SearchElasticsearchRepository repository) {
        return args -> {
            logger.info("Initializing Elasticsearch indices and aliases...");
            try {
                repository.createIndex();
                repository.createIndexAlias();
                logger.info("Elasticsearch initialization completed successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize Elasticsearch: {}", e.getMessage(), e);
            }
        };
    }
}
