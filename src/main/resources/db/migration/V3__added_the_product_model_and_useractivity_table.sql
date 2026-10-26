CREATE TABLE order_items
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_sku  VARCHAR(64)    NOT NULL,
    product_name VARCHAR(200)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(12, 2) NOT NULL,
    line_total   DECIMAL(12, 2) NOT NULL,
    currency     VARCHAR(3)     NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id)
);

CREATE TABLE products
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    sku           VARCHAR(64)    NOT NULL,
    name          VARCHAR(200)   NOT NULL,
    `description` VARCHAR(2000) NULL,
    price         DECIMAL(12, 2) NOT NULL,
    currency      VARCHAR(3)     NOT NULL,
    active        BIT(1)         NOT NULL,
    version       BIGINT NULL,
    created_at    datetime(6)           NOT NULL,
    updated_at    datetime(6)           NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

ALTER TABLE user_activity
    ADD app_id VARCHAR(64) NULL;

ALTER TABLE user_activity
    ADD lead_id BIGINT NULL;

ALTER TABLE user_activity
    ADD order_id BIGINT NULL;

ALTER TABLE user_activity
    ADD payment_id BIGINT NULL;

ALTER TABLE user_activity
    ADD product_id BIGINT NULL;

ALTER TABLE user_activity
    ADD properties JSON NULL;

ALTER TABLE user_activity
    ADD schema_version INT NULL;

ALTER TABLE user_activity
    ADD session_id VARCHAR(100) NULL;

ALTER TABLE user_activity
    ADD source VARCHAR(20) NULL;

ALTER TABLE user_activity
    MODIFY app_id VARCHAR (64) NOT NULL;

ALTER TABLE user_activity
    MODIFY properties JSON NOT NULL;

ALTER TABLE user_activity
    MODIFY schema_version INT NOT NULL;

ALTER TABLE user_activity
    MODIFY source VARCHAR (20) NOT NULL;

ALTER TABLE order_items
    ADD CONSTRAINT uk_order_items_order_product UNIQUE (order_id, product_id);

ALTER TABLE products
    ADD CONSTRAINT uk_products_sku UNIQUE (sku);

CREATE INDEX idx_activity_product_time ON user_activity (product_id, occurred_at);

CREATE INDEX idx_activity_type_time ON user_activity (event_type, occurred_at);

CREATE INDEX idx_activity_user_time ON user_activity (user_id, occurred_at);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE user_activity
    ADD CONSTRAINT FK_USER_ACTIVITY_ON_LEAD FOREIGN KEY (lead_id) REFERENCES leads (id);

ALTER TABLE user_activity
    ADD CONSTRAINT FK_USER_ACTIVITY_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE user_activity
    ADD CONSTRAINT FK_USER_ACTIVITY_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payments (id);

ALTER TABLE user_activity
    ADD CONSTRAINT FK_USER_ACTIVITY_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);