CREATE SEQUENCE IF NOT EXISTS outbox_sequence START WITH 1;

CREATE TABLE IF NOT EXISTS outbox_events (
    id              BIGINT          DEFAULT nextval('outbox_sequence') PRIMARY KEY,
    topic           VARCHAR(255)    NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    payload         TEXT            NOT NULL,
    quote_id        BIGINT,
    flag_sent       BOOLEAN         NOT NULL DEFAULT FALSE,
    dat_sent        TIMESTAMP,
    dat_received    TIMESTAMP,
    dat_created     TIMESTAMP       NOT NULL DEFAULT NOW(),
    dat_updated     TIMESTAMP       NOT NULL DEFAULT NOW(),
    retry_count     INT             NOT NULL DEFAULT 0,
    status          VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    error_message   TEXT
);

CREATE INDEX IF NOT EXISTS idx_outbox_flag_sent_status ON outbox_events (flag_sent, status);
CREATE INDEX IF NOT EXISTS idx_outbox_quote_id ON outbox_events (quote_id);
