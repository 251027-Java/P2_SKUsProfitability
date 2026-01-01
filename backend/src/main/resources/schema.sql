-- Active: 1767131972636@@127.0.0.1@5432@sku_profitability_db
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
    product_name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    length NUMERIC(10, 2),
    width NUMERIC(10, 2),
    height NUMERIC(10, 2),
    weight NUMERIC(10, 2),
    category VARCHAR(50) NOT NULL,
    selling_price NUMERIC(10, 2),
    size_classification VARCHAR(50),
    fba_fulfillment_fee NUMERIC(10, 2),
    referral_fee NUMERIC(10, 2),
    storage_fee NUMERIC(10, 2),
    total_fees NUMERIC(10, 2),
    net_profit NUMERIC(10, 2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_skus_sku UNIQUE (sku)
);

DO $$ 
DECLARE
    table_exists BOOLEAN;
BEGIN
    -- Check if table exists
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'skus'
    ) INTO table_exists;
    
    IF table_exists THEN
        -- Add description column if it doesn't exist (as nullable first, will be set to NOT NULL later)
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'skus' AND column_name = 'description'
        ) THEN
            ALTER TABLE skus ADD COLUMN description TEXT;
            -- Set default value for any existing rows
            UPDATE skus SET description = 'No description provided' WHERE description IS NULL;
        END IF;
        
        -- Add size_classification column if it doesn't exist
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'skus' AND column_name = 'size_classification'
        ) THEN
            ALTER TABLE skus ADD COLUMN size_classification VARCHAR(50);
        END IF;
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

-- Remove cost, target_roi, and max_cost columns if they exist
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'skus' AND column_name = 'cost'
    ) THEN
        ALTER TABLE skus DROP COLUMN cost;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'skus' AND column_name = 'target_roi'
    ) THEN
        ALTER TABLE skus DROP COLUMN target_roi;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'skus' AND column_name = 'max_cost'
    ) THEN
        ALTER TABLE skus DROP COLUMN max_cost;
    END IF;
END $$;

-- Handle existing data migration before adding constraints
-- Only run if the skus table already exists
DO $$ 
DECLARE
    table_exists BOOLEAN;
    duplicate_count INTEGER;
    null_product_name_count INTEGER;
    null_description_count INTEGER;
    null_category_count INTEGER;
BEGIN
    -- Check if table exists
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'skus'
    ) INTO table_exists;
    
    -- Only run migration if table exists
    IF table_exists THEN
        -- Check for duplicate SKUs before adding unique constraint
        SELECT COUNT(*) INTO duplicate_count
        FROM (
            SELECT sku, COUNT(*) as cnt
            FROM skus
            GROUP BY sku
            HAVING COUNT(*) > 1
        ) duplicates;
        
        IF duplicate_count > 0 THEN
            RAISE EXCEPTION 'Cannot add unique constraint: Found % duplicate SKU(s). Please resolve duplicates before migration.', duplicate_count;
        END IF;
        
        -- Check for NULL values in product_name
        SELECT COUNT(*) INTO null_product_name_count
        FROM skus
        WHERE product_name IS NULL;
        
        -- Set default values for NULL product_name
        IF null_product_name_count > 0 THEN
            UPDATE skus
            SET product_name = 'Unnamed Product'
            WHERE product_name IS NULL;
            RAISE NOTICE 'Updated % record(s) with NULL product_name to default value', null_product_name_count;
        END IF;
        
        -- Check for NULL values in description
        SELECT COUNT(*) INTO null_description_count
        FROM skus
        WHERE description IS NULL;
        
        -- Set default values for NULL description
        IF null_description_count > 0 THEN
            UPDATE skus
            SET description = 'No description provided'
            WHERE description IS NULL;
            RAISE NOTICE 'Updated % record(s) with NULL description to default value', null_description_count;
        END IF;
        
        -- Check for NULL values in category
        SELECT COUNT(*) INTO null_category_count
        FROM skus
        WHERE category IS NULL;
        
        -- Set default values for NULL category
        IF null_category_count > 0 THEN
            UPDATE skus
            SET category = 'Uncategorized'
            WHERE category IS NULL;
            RAISE NOTICE 'Updated % record(s) with NULL category to default value', null_category_count;
        END IF;
    END IF;
END $$;

-- Add unique constraint on SKU if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uk_skus_sku' AND table_name = 'skus'
    ) THEN
        ALTER TABLE skus ADD CONSTRAINT uk_skus_sku UNIQUE (sku);
        RAISE NOTICE 'Added unique constraint on sku column';
    END IF;
END $$;

-- Update existing columns to be NOT NULL if they are currently nullable
DO $$ 
BEGIN
    -- Make product_name NOT NULL
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'skus' AND column_name = 'product_name' AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE skus ALTER COLUMN product_name SET NOT NULL;
        RAISE NOTICE 'Set product_name column to NOT NULL';
    END IF;
    
    -- Make description NOT NULL
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'skus' AND column_name = 'description' AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE skus ALTER COLUMN description SET NOT NULL;
        RAISE NOTICE 'Set description column to NOT NULL';
    END IF;
    
    -- Make category NOT NULL
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'skus' AND column_name = 'category' AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE skus ALTER COLUMN category SET NOT NULL;
        RAISE NOTICE 'Set category column to NOT NULL';
    END IF;
END $$;

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
