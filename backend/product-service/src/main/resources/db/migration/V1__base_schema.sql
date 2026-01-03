CREATE TABLE app_users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    user_role VARCHAR(50) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

CREATE INDEX idx_app_users_email ON app_users(email);

INSERT INTO app_users (email, password, user_role, first_name, last_name)
VALUES ('admin@test.com', 'password', 'ADMIN', 'Admin', 'User')
ON CONFLICT (email) DO NOTHING;

CREATE TABLE skus (
    sku_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
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
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_skus_user FOREIGN KEY (user_id)
        REFERENCES app_users(user_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_skus_user_id ON skus(user_id);
CREATE INDEX idx_skus_sku ON skus(sku);
CREATE INDEX idx_skus_category ON skus(category);
CREATE INDEX idx_skus_product_name ON skus(product_name);
