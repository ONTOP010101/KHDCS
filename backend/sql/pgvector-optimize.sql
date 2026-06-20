-- ============================================
-- pgvector 优化脚本: 去掉 shard_prefix，添加 HNSW 索引
-- ============================================

-- 1. 重建 image_vectors 表（去掉 shard_prefix 列，简化主键）
-- 如果已有数据且需要保留，请使用手动迁移
DROP TABLE IF EXISTS image_vectors;

CREATE TABLE image_vectors (
    image_id BIGINT NOT NULL PRIMARY KEY,
    embedding vector(1280)
);

-- 2. 创建 HNSW 索引（替代全表扫描，查询速度提升 50-500 倍）
-- 需要 pgvector 0.5.0+ 版本支持 HNSW
CREATE INDEX ON image_vectors USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 200);

-- 3. 如需重建数据，使用后端 API: POST /images/backfill-pgvector
