CREATE TABLE leads
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    user_id    BIGINT       NOT NULL,
    source     VARCHAR(100) NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    created_at datetime(6)           NOT NULL,
    updated_at datetime(6)           NOT NULL,
    CONSTRAINT pk_leads PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    user_id     BIGINT         NOT NULL,
    lead_id     BIGINT         NOT NULL,
    amount      DECIMAL(12, 2) NOT NULL,
    status      VARCHAR(20)    NOT NULL,
    created_at  datetime(6)           NOT NULL,
    updated_at  datetime(6)           NOT NULL,
    paid_at     datetime(6)           NULL,
    refunded_at datetime(6)           NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE payments
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    order_id           BIGINT         NOT NULL,
    provider_reference VARCHAR(100)   NOT NULL,
    amount             DECIMAL(12, 2) NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    created_at         datetime(6)           NOT NULL,
    refunded_at        datetime(6)           NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

CREATE TABLE revchanges
(
    rev        BIGINT NOT NULL,
    entityname VARCHAR(255) NULL
);

CREATE TABLE revinfo
(
    rev      BIGINT NOT NULL,
    revtstmp BIGINT NULL,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE TABLE user_activity
(
    event_id    VARCHAR(36)  NOT NULL,
    user_id     BIGINT       NOT NULL,
    event_type  VARCHAR(100) NOT NULL,
    occurred_at datetime(6)  NOT NULL,
    received_at datetime(6)  NOT NULL,
    CONSTRAINT pk_user_activity PRIMARY KEY (event_id)
);

CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime(6)           NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE payments
    ADD CONSTRAINT uc_payments_order UNIQUE (order_id);

ALTER TABLE payments
    ADD CONSTRAINT uc_payments_provider_reference UNIQUE (provider_reference);

ALTER TABLE leads
    ADD CONSTRAINT FK_LEADS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_LEAD FOREIGN KEY (lead_id) REFERENCES leads (id);

ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE payments
    ADD CONSTRAINT FK_PAYMENTS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE user_activity
    ADD CONSTRAINT FK_USER_ACTIVITY_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE revchanges
    ADD CONSTRAINT fk_revchanges_on_default_tracking_modified_entities_changelog FOREIGN KEY (rev) REFERENCES revinfo (rev);