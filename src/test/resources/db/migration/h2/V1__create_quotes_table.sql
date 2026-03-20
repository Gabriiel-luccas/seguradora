CREATE SEQUENCE IF NOT EXISTS quote_sequence START WITH 1;

CREATE TABLE IF NOT EXISTS quotes (
    id              BIGINT          DEFAULT nextval('quote_sequence') PRIMARY KEY,
    product_id      VARCHAR(255)    NOT NULL,
    offer_id        VARCHAR(255)    NOT NULL,
    category        VARCHAR(255)    NOT NULL,
    total_monthly_premium_amount    DECIMAL(15, 2),
    total_coverage_amount           DECIMAL(15, 2),
    coverages       TEXT,
    assistances     TEXT,
    customer        TEXT,
    policy_id       BIGINT,
    status          VARCHAR(50),
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);
