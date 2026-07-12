CREATE TABLE IF NOT EXISTS outbox_event (
    id             BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(100) NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSONB        NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count    INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ
);

-- PENDING만 스캔하는 부분 인덱스: 폴링 쿼리 최적화
CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON outbox_event (id)
    WHERE status = 'PENDING';
