CREATE SEQUENCE IF NOT EXISTS customer_sequence   START WITH 1;
CREATE SEQUENCE IF NOT EXISTS coverage_sequence   START WITH 1;
CREATE SEQUENCE IF NOT EXISTS assistance_sequence START WITH 1;

CREATE TABLE customers (
    id               BIGINT        DEFAULT nextval('customer_sequence') PRIMARY KEY,
    document_number  VARCHAR(20)   NOT NULL,
    name             VARCHAR(255)  NOT NULL,
    type             VARCHAR(20)   NOT NULL,
    gender           VARCHAR(20),
    date_of_birth    VARCHAR(20),
    email            VARCHAR(255),
    phone_number     VARCHAR(20)
);

CREATE TABLE quote_coverages (
    id        BIGINT          DEFAULT nextval('coverage_sequence') PRIMARY KEY,
    quote_id  BIGINT          NOT NULL,
    name      VARCHAR(255)    NOT NULL,
    value     DECIMAL(15, 2)  NOT NULL,
    CONSTRAINT fk_coverage_quote FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE CASCADE
);

CREATE TABLE quote_assistances (
    id        BIGINT        DEFAULT nextval('assistance_sequence') PRIMARY KEY,
    quote_id  BIGINT        NOT NULL,
    name      VARCHAR(255)  NOT NULL,
    CONSTRAINT fk_assistance_quote FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE CASCADE
);

ALTER TABLE quotes ADD COLUMN customer_id BIGINT;
ALTER TABLE quotes ADD CONSTRAINT fk_quote_customer FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE quotes DROP COLUMN coverages;
ALTER TABLE quotes DROP COLUMN assistances;
ALTER TABLE quotes DROP COLUMN customer;

CREATE INDEX idx_coverage_quote_id   ON quote_coverages   (quote_id);
CREATE INDEX idx_assistance_quote_id ON quote_assistances (quote_id);
CREATE INDEX idx_customer_document   ON customers         (document_number);

