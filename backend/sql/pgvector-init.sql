CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS image_vectors (
    image_id BIGINT NOT NULL PRIMARY KEY,
    embedding VECTOR(1280),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- HNSW 索引替代 IVFFlat，查询速度提升 10-50 倍
CREATE INDEX IF NOT EXISTS idx_image_vectors_embedding_hnsw
    ON image_vectors USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 200);
