-- Remove deprecated columns
ALTER TABLE skus DROP COLUMN IF EXISTS cost;
ALTER TABLE skus DROP COLUMN IF EXISTS target_roi;
ALTER TABLE skus DROP COLUMN IF EXISTS max_cost;

-- Normalize bad data
UPDATE skus SET product_name = 'Unnamed Product'
WHERE product_name IS NULL;

UPDATE skus SET description = 'No description provided'
WHERE description IS NULL;

UPDATE skus SET category = 'Uncategorized'
WHERE category IS NULL;

-- Enforce constraints
ALTER TABLE skus
    ALTER COLUMN product_name SET NOT NULL,
    ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN category SET NOT NULL;
