-- Remove app_users table and related code
CREATE TABLE skus (
    sku_id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    length NUMERIC(10,2),
    width NUMERIC(10,2),
    height NUMERIC(10,2),
    weight NUMERIC(10,2),
    category VARCHAR(50) NOT NULL,
    selling_price NUMERIC(10,2),
    size_classification VARCHAR(50),
    fba_fulfillment_fee NUMERIC(10,2),
    referral_fee NUMERIC(10,2),
    storage_fee NUMERIC(10,2),
    total_fees NUMERIC(10,2),
    net_profit NUMERIC(10,2),
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_skus_sku ON skus(sku);
CREATE INDEX idx_skus_category ON skus(category);
CREATE INDEX idx_skus_product_name ON skus(product_name);