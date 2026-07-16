package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 配置类
 * 通过 elasticsearch.enabled 配置动态控制是否启用 ES
 */
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "com.app.es.repository")
public class ElasticsearchConfig {

    @Value("${elasticsearch.index.name:samples}")
    private String indexName;

    @Bean("esIndexName")
    public String esIndexName() {
        return indexName;
    }
}
