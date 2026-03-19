CREATE SEQUENCE IF NOT EXISTS quote_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS quotes (
    id BIGINT PRIMARY KEY DEFAULT nextval('quote_sequence'),
    product_id VARCHAR(255) NOT NULL,
    offer_id VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    total_monthly_premium_amount DECIMAL(15, 2) NOT NULL,
    total_coverage_amount DECIMAL(15, 2) NOT NULL,
    coverages TEXT,
    assistances TEXT,
    customer TEXT,
    policy_id BIGINT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
