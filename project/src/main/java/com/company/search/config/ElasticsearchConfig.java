package com.company.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.scheme:http}")
    private String scheme;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public RestClient restClient() {
        logger.info("Creating Elasticsearch REST client for {}://{}:{}", scheme, host, port);

        RestClientBuilder builder = RestClient.builder(
                new HttpHost(host, port, scheme)
        );

        // Configuration des timeouts pour éviter les erreurs
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(5000)    // 5 secondes
                        .setSocketTimeout(30000)    // 30 secondes
        );

        // Configuration du pool de connexions
        builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder
                        .setMaxConnPerRoute(10)
                        .setMaxConnTotal(30)
        );

        RestClient client = builder.build();
        logger.info("Elasticsearch REST client created successfully");

        return client;
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        logger.info("Creating Elasticsearch transport with configured ObjectMapper");

        // Utiliser l'ObjectMapper configuré dans JacksonConfig
        // qui a le support des dates Java 8 (JavaTimeModule)
        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);

        return new RestClientTransport(restClient, jsonpMapper);
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        logger.info("Creating Elasticsearch client");

        ElasticsearchClient client = new ElasticsearchClient(transport);
        logger.info("Elasticsearch client created successfully");

        return client;
    }
}