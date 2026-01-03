CREATE TABLE lists (
    list_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_lists_user FOREIGN KEY (user_id)
        REFERENCES app_users(user_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_lists_user_id ON lists(user_id);

CREATE TABLE list_items (
    list_item_id BIGSERIAL PRIMARY KEY,
    list_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_list_items_list FOREIGN KEY (list_id)
        REFERENCES lists(list_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_list_items_sku FOREIGN KEY (sku_id)
        REFERENCES skus(sku_id)
        ON DELETE CASCADE,
    CONSTRAINT uk_list_items_unique UNIQUE (list_id, sku_id)
);

CREATE INDEX idx_list_items_list_id ON list_items(list_id);
CREATE INDEX idx_list_items_sku_id ON list_items(sku_id);
