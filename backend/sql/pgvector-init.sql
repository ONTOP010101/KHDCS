CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS image_vectors (
    image_id BIGINT PRIMARY KEY,
    embedding VECTOR,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_image_vectors_embedding
    ON image_vectors USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 200);
