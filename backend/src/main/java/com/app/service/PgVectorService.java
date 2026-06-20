package com.app.service;

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
    private volatile long availableCheckTime = 0;
    private static final long AVAILABLE_TTL_MS = 30_000;

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
        pgDataSource.setMaximumPoolSize(5);
        pgDataSource.setMinimumIdle(1);
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
        long now = System.currentTimeMillis();
        if (available != null && (now - availableCheckTime) < AVAILABLE_TTL_MS) {
            return available;
        }
        try {
            pgJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (available == null || !available) {
                log.info("PgVectorService became available");
            }
            available = true;
        } catch (Exception e) {
            if (available == null || available) {
                log.warn("PgVectorService is not available: {}", e.getMessage());
            }
            available = false;
        }
        availableCheckTime = now;
        return available;
    }

    public int upsert(long imageId, float[] embedding) {
        if (!isAvailable() || embedding == null || embedding.length == 0) return 0;
        try {
            PGvector vec = new PGvector(embedding);
            log.debug("Upserting image_id={}, embedding_len={}", imageId, embedding.length);
            int rows = pgJdbcTemplate.update(
                    "INSERT INTO image_vectors (image_id, embedding) VALUES (?, ?) ON CONFLICT (image_id) DO UPDATE SET embedding = EXCLUDED.embedding",
                    imageId, vec);
            log.debug("Upsert result: image_id={}, rows={}", imageId, rows);
            return rows;
        } catch (Exception e) {
            log.error("Failed to upsert vector for image_id={}, error={}", imageId, e.getMessage(), e);
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

    public void delete(long imageId) {
        if (!isAvailable()) return;
        try {
            pgJdbcTemplate.update("DELETE FROM image_vectors WHERE image_id = ?", imageId);
        } catch (Exception e) {
            log.warn("Failed to delete vector for image {}: {}", imageId, e.getMessage());
        }
    }

    public List<Map<String, Object>> searchSimilar(float[] queryEmbedding, int limit, double minSimilarity) {
        if (!isAvailable() || queryEmbedding == null || queryEmbedding.length == 0) {
            return Collections.emptyList();
        }
        try {
            pgJdbcTemplate.execute("SET hnsw.ef_search = 200");
            PGvector vec = new PGvector(queryEmbedding);
            return pgJdbcTemplate.query(
                    "SELECT image_id, 1.0 - (embedding <=> ?) AS similarity " +
                            "FROM image_vectors WHERE embedding <=> ? <= ? " +
                            "ORDER BY embedding <=> ? LIMIT ?",
                    (rs, rowNum) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("imageId", rs.getLong("image_id"));
                        row.put("similarity", rs.getDouble("similarity"));
                        return row;
                    },
                    vec, vec, 1.0 - minSimilarity, vec, limit);
        } catch (Exception e) {
            log.warn("PgVector search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean exists(long imageId) {
        if (!isAvailable()) return false;
        try {
            Integer count = pgJdbcTemplate.queryForObject("SELECT COUNT(*) FROM image_vectors WHERE image_id = ?", Integer.class, imageId);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
