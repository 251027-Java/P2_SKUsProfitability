-- =========================================================
-- V4: Normalize SKUs by removing user_id and adding join table
-- =========================================================

-- 1. Create join table to map users to SKUs
CREATE TABLE user_skus (
    user_sku_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_user_skus_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_skus_sku
        FOREIGN KEY (sku_id)
        REFERENCES skus(sku_id)
        ON DELETE CASCADE,

    CONSTRAINT uk_user_skus_user_sku
        UNIQUE (user_id, sku_id)
);

-- 2. Migrate existing user → SKU relationships into join table
INSERT INTO user_skus (
    user_id,
    sku_id,
    created_at,
    updated_at
)
SELECT
    user_id,
    sku_id,
    created_at,
    updated_at
FROM skus;

-- 3. Drop foreign key constraint from skus.user_id
ALTER TABLE skus
    DROP CONSTRAINT fk_skus_user;

-- 4. Remove user_id column from skus
ALTER TABLE skus
    DROP COLUMN user_id;

-- 5. Add indexes for fast joins
CREATE INDEX idx_user_skus_user_id ON user_skus(user_id);
CREATE INDEX idx_user_skus_sku_id ON user_skus(sku_id);