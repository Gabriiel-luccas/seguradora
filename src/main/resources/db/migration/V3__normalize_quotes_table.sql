-- ============================================================
-- V3: Normalize quotes — separate coverages, assistances and
--     customer into their own tables
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS customer_sequence   START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS coverage_sequence   START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS assistance_sequence START WITH 1 INCREMENT BY 1;

-- 1. customers -----------------------------------------------
CREATE TABLE customers (
    id               BIGINT        PRIMARY KEY DEFAULT nextval('customer_sequence'),
    document_number  VARCHAR(20)   NOT NULL,
    name             VARCHAR(255)  NOT NULL,
    type             VARCHAR(20)   NOT NULL,
    gender           VARCHAR(20),
    date_of_birth    VARCHAR(20),
    email            VARCHAR(255),
    phone_number     VARCHAR(20)
);

-- 2. quote_coverages ------------------------------------------
CREATE TABLE quote_coverages (
    id        BIGINT          PRIMARY KEY DEFAULT nextval('coverage_sequence'),
    quote_id  BIGINT          NOT NULL,
    name      VARCHAR(255)    NOT NULL,
    value     DECIMAL(15, 2)  NOT NULL,
    CONSTRAINT fk_coverage_quote FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE CASCADE
);

-- 3. quote_assistances ----------------------------------------
CREATE TABLE quote_assistances (
    id        BIGINT        PRIMARY KEY DEFAULT nextval('assistance_sequence'),
    quote_id  BIGINT        NOT NULL,
    name      VARCHAR(255)  NOT NULL,
    CONSTRAINT fk_assistance_quote FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE CASCADE
);

-- 4. Add customer_id column to quotes -------------------------
ALTER TABLE quotes ADD COLUMN customer_id BIGINT;

-- 5. Migrate existing data from JSON TEXT columns -------------
DO $$
DECLARE
    rec      RECORD;
    cust_id  BIGINT;
    coverage JSONB;
    asst     TEXT;
BEGIN
    FOR rec IN
        SELECT id,
               customer::jsonb    AS customer,
               coverages::jsonb   AS coverages,
               assistances::jsonb AS assistances
        FROM quotes
        WHERE customer IS NOT NULL
    LOOP
        -- migrate customer
        INSERT INTO customers (document_number, name, type, gender, date_of_birth, email, phone_number)
        VALUES (
            rec.customer->>'documentNumber',
            rec.customer->>'name',
            rec.customer->>'type',
            rec.customer->>'gender',
            rec.customer->>'dateOfBirth',
            rec.customer->>'email',
            rec.customer->>'phoneNumber'
        ) RETURNING id INTO cust_id;

        UPDATE quotes SET customer_id = cust_id WHERE id = rec.id;

        -- migrate coverages
        FOR coverage IN SELECT jsonb_array_elements(rec.coverages) LOOP
            INSERT INTO quote_coverages (quote_id, name, value)
            VALUES (rec.id, coverage->>'name', (coverage->>'value')::DECIMAL);
        END LOOP;

        -- migrate assistances
        FOR asst IN SELECT jsonb_array_elements_text(rec.assistances) LOOP
            INSERT INTO quote_assistances (quote_id, name)
            VALUES (rec.id, asst);
        END LOOP;
    END LOOP;
END $$;

-- 6. Add FK constraint after data migration -------------------
ALTER TABLE quotes ADD CONSTRAINT fk_quote_customer
    FOREIGN KEY (customer_id) REFERENCES customers(id);

-- 7. Drop old TEXT columns ------------------------------------
ALTER TABLE quotes DROP COLUMN IF EXISTS coverages;
ALTER TABLE quotes DROP COLUMN IF EXISTS assistances;
ALTER TABLE quotes DROP COLUMN IF EXISTS customer;

-- 8. Indexes --------------------------------------------------
CREATE INDEX idx_coverage_quote_id   ON quote_coverages   (quote_id);
CREATE INDEX idx_assistance_quote_id ON quote_assistances (quote_id);
CREATE INDEX idx_customer_document   ON customers         (document_number);

