package com.app.service;

import com.app.entity.Image;
import com.pgvector.PGvector;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnExpression("'${pgvector.enabled:false}' == 'true'")
public class PgVectorService {

    private static final Logger log = LoggerFactory.getLogger(PgVectorService.class);

    @Autowired
    private com.app.config.PgVectorConfig pgVectorConfig;

    private JdbcTemplate pgJdbcTemplate;
    private HikariDataSource pgDataSource;
    private volatile Boolean available;

    @PostConstruct
    public void init() {
        log.info("PgVectorService init: enabled={}, url={}, user={}", pgVectorConfig.isEnabled(), pgVectorConfig.getUrl(), pgVectorConfig.getUsername());
        if (!pgVectorConfig.isEnabled()) {
            log.info("PgVectorService disabled by config");
            return;
        }
        String dbUrl = pgVectorConfig.getUrl();
        if (dbUrl == null || dbUrl.isBlank()) {
            log.warn("PgVectorService URL is empty, skipping init");
            return;
        }
        pgDataSource = new HikariDataSource();
        pgDataSource.setJdbcUrl(dbUrl);
        pgDataSource.setUsername(pgVectorConfig.getUsername());
        pgDataSource.setPassword(pgVectorConfig.getPassword());
        pgDataSource.setAutoCommit(true);
        pgDataSource.setMaximumPoolSize(20);
        pgDataSource.setMinimumIdle(2);
        pgDataSource.setConnectionTimeout(10000);
        pgDataSource.setIdleTimeout(300000);
        pgDataSource.setMaxLifetime(600000);
        pgDataSource.setConnectionTestQuery("SELECT 1");
        pgJdbcTemplate = new JdbcTemplate(pgDataSource);
        log.info("PgVectorService initialized with url={}", dbUrl);
    }

    @PreDestroy
    public void cleanup() {
        if (pgDataSource != null) {
            pgDataSource.close();
        }
    }

    public boolean isAvailable() {
        if (!pgVectorConfig.isEnabled()) return false;
        if (available != null) return available;
        try {
            pgJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            available = true;
            log.info("PgVectorService is available");
        } catch (Exception e) {
            available = false;
            log.warn("PgVectorService is not available: {}", e.getMessage());
        }
        return available;
    }

    public int upsert(long imageId, String shardPrefix, float[] embedding) {
        if (!isAvailable() || embedding == null || embedding.length == 0) return 0;
        String prefix = (shardPrefix == null || shardPrefix.isEmpty()) ? "00" : shardPrefix;
        try {
            PGvector vec = new PGvector(embedding);
            log.debug("Upserting image_id={}, shard={}, embedding_len={}", imageId, prefix, embedding.length);
            int rows = pgJdbcTemplate.update(
                    "INSERT INTO image_vectors (image_id, shard_prefix, embedding) VALUES (?, ?, ?) ON CONFLICT (image_id, shard_prefix) DO UPDATE SET embedding = EXCLUDED.embedding",
                    imageId, prefix, vec);
            log.debug("Upsert result: image_id={}, shard={}, rows={}", imageId, prefix, rows);
            return rows;
        } catch (Exception e) {
            log.error("Failed to upsert vector for image_id={}, shard={}, error={}", imageId, shardPrefix, e.getMessage(), e);
            return 0;
        }
    }

    public int count() {
        if (!isAvailable()) return 0;
        try {
            Integer c = pgJdbcTemplate.queryForObject("SELECT COUNT(*) FROM image_vectors", Integer.class);
            return c != null ? c : 0;
        } catch (Exception e) {
            log.warn("PgVector count failed: {}", e.getMessage());
            return 0;
        }
    }

    public void delete(long imageId, String shardPrefix) {
        if (!isAvailable()) return;
        String prefix = (shardPrefix == null || shardPrefix.isEmpty()) ? "00" : shardPrefix;
        try {
            pgJdbcTemplate.update("DELETE FROM image_vectors WHERE image_id = ? AND shard_prefix = ?", imageId, prefix);
        } catch (Exception e) {
            log.warn("Failed to delete vector for image {} shard {}: {}", imageId, prefix, e.getMessage());
        }
    }

    public List<Map<String, Object>> searchSimilar(float[] queryEmbedding, int limit, double minSimilarity) {
        if (!isAvailable() || queryEmbedding == null || queryEmbedding.length == 0) {
            return Collections.emptyList();
        }
        try {
            PGvector vec = new PGvector(queryEmbedding);
            double maxDistance = 1.0 - minSimilarity;
            return pgJdbcTemplate.query(
                    "SELECT image_id, shard_prefix FROM image_vectors " +
                            "WHERE embedding <=> ? <= ? " +
                            "ORDER BY embedding <=> ? LIMIT ?",
                    (rs, rowNum) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("imageId", rs.getLong("image_id"));
                        row.put("shardPrefix", rs.getString("shard_prefix"));
                        return row;
                    },
                    vec, maxDistance, vec, limit);
        } catch (Exception e) {
            log.warn("PgVector search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }


    public boolean exists(long imageId, String shardPrefix) {
        if (!isAvailable()) return false;
        try {
            Integer count = pgJdbcTemplate.queryForObject("SELECT COUNT(*) FROM image_vectors WHERE image_id = ? AND shard_prefix = ?", Integer.class, imageId, shardPrefix);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }


}
