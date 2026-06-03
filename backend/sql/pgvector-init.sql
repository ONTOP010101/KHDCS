CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS image_vectors (
    image_id BIGINT NOT NULL,
    shard_prefix VARCHAR(2) NOT NULL,
    embedding VECTOR(1280),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (image_id, shard_prefix)
);

CREATE INDEX IF NOT EXISTS idx_image_vectors_embedding
    ON image_vectors USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 200);
