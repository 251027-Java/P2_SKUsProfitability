CREATE TABLE IF NOT EXISTS app_users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    user_role VARCHAR(50) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_app_users_email ON app_users(email);

CREATE TABLE IF NOT EXISTS skus (
    sku_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(200),
    description TEXT,
    length NUMERIC(10, 2),
    width NUMERIC(10, 2),
    height NUMERIC(10, 2),
    weight NUMERIC(10, 2),
    category VARCHAR(50),
    selling_price NUMERIC(10, 2),
    cost NUMERIC(10, 2),
    target_roi NUMERIC(10, 2),
    size_classification VARCHAR(50),
    fba_fulfillment_fee NUMERIC(10, 2),
    referral_fee NUMERIC(10, 2),
    storage_fee NUMERIC(10, 2),
    total_fees NUMERIC(10, 2),
    net_profit NUMERIC(10, 2),
    max_cost NUMERIC(10, 2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'skus' AND column_name = 'description'
    ) THEN
        ALTER TABLE skus ADD COLUMN description TEXT;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'skus' AND column_name = 'size_classification'
    ) THEN
        ALTER TABLE skus ADD COLUMN size_classification VARCHAR(50);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_skus_user' AND table_name = 'skus'
    ) THEN
        ALTER TABLE skus ADD CONSTRAINT fk_skus_user FOREIGN KEY (user_id) REFERENCES app_users(user_id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_skus_user_id ON skus(user_id);
CREATE INDEX IF NOT EXISTS idx_skus_sku ON skus(sku);
CREATE INDEX IF NOT EXISTS idx_skus_category ON skus(category);
CREATE INDEX IF NOT EXISTS idx_skus_size_classification ON skus(size_classification);
CREATE INDEX IF NOT EXISTS idx_skus_product_name ON skus(product_name);

CREATE TABLE IF NOT EXISTS lists (
    list_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_lists_user' AND table_name = 'lists'
    ) THEN
        ALTER TABLE lists ADD CONSTRAINT fk_lists_user FOREIGN KEY (user_id) REFERENCES app_users(user_id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_lists_user_id ON lists(user_id);

CREATE TABLE IF NOT EXISTS list_items (
    list_item_id BIGSERIAL PRIMARY KEY,
    list_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_list_items_list' AND table_name = 'list_items'
    ) THEN
        ALTER TABLE list_items ADD CONSTRAINT fk_list_items_list FOREIGN KEY (list_id) REFERENCES lists(list_id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_list_items_sku' AND table_name = 'list_items'
    ) THEN
        ALTER TABLE list_items ADD CONSTRAINT fk_list_items_sku FOREIGN KEY (sku_id) REFERENCES skus(sku_id) ON DELETE CASCADE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uk_list_items_unique' AND table_name = 'list_items'
    ) THEN
        ALTER TABLE list_items ADD CONSTRAINT uk_list_items_unique UNIQUE (list_id, sku_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_list_items_list_id ON list_items(list_id);
CREATE INDEX IF NOT EXISTS idx_list_items_sku_id ON list_items(sku_id);
