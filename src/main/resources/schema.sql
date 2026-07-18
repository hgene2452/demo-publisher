CREATE TABLE IF NOT EXISTS outbox_event (
                                            id             BIGSERIAL PRIMARY KEY,
                                            aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(100) NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSONB        NOT NULL,
    status         VARCHAR(30)  NOT NULL DEFAULT 'MINT_REQUEST_PENDING',
    retry_count    INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ
    );

CREATE INDEX IF NOT EXISTS idx_outbox_mint_request_pending
    ON outbox_event (id) WHERE status = 'MINT_REQUEST_PENDING';

CREATE INDEX IF NOT EXISTS idx_outbox_mint_confirm_pending
    ON outbox_event (id) WHERE status = 'MINT_CONFIRM_PENDING';
