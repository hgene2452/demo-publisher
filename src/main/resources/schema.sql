CREATE TABLE IF NOT EXISTS outbox_event (
                                            id             BIGSERIAL PRIMARY KEY,   -- 삽입 순서 + Kafka 헤더의 eventId
                                            sequence_type  VARCHAR(20)  NOT NULL,   -- MINT / BURN / TRANSFER
    aggregate_type VARCHAR(100) NOT NULL,   -- 도메인 구분 (예: Remittance)
    aggregate_id   VARCHAR(100) NOT NULL,   -- Kafka 파티션 key → 같은 건은 같은 파티션
    event_type     VARCHAR(100) NOT NULL,   -- 원본 타입 (발행 시엔 스테이지 값이 우선)
    payload        JSONB        NOT NULL,   -- 이벤트 본문
    status         VARCHAR(40)  NOT NULL,   -- 상태머신의 현재 위치 (INSERT 시 명시)
    retry_count    INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ              -- 마지막 발행 시각
    );

-- 부분 인덱스: "폴링 대상 상태"만 인덱싱한다.
-- 완료된 행은 인덱스에 아예 없으므로 테이블이 커져도 폴링 쿼리는 가볍다.
-- 조건(status)은 WHERE절이, 정렬(ORDER BY id)은 인덱스 키가 해결 → 정렬 없이 앞에서 n개.
-- TODO: 스테이지를 추가하면 여기에도 부분 인덱스를 추가할 것
CREATE INDEX IF NOT EXISTS idx_outbox_mint_request_pending
    ON outbox_event (id) WHERE status = 'MINT_REQUEST_PENDING';
CREATE INDEX IF NOT EXISTS idx_outbox_mint_confirm_pending
    ON outbox_event (id) WHERE status = 'MINT_CONFIRM_PENDING';
CREATE INDEX IF NOT EXISTS idx_outbox_burn_request_pending
    ON outbox_event (id) WHERE status = 'BURN_REQUEST_PENDING';
CREATE INDEX IF NOT EXISTS idx_outbox_burn_confirm_pending
    ON outbox_event (id) WHERE status = 'BURN_CONFIRM_PENDING';
CREATE INDEX IF NOT EXISTS idx_outbox_transfer_request_pending
    ON outbox_event (id) WHERE status = 'TRANSFER_REQUEST_PENDING';
CREATE INDEX IF NOT EXISTS idx_outbox_transfer_confirm_pending
    ON outbox_event (id) WHERE status = 'TRANSFER_CONFIRM_PENDING';

-- 운영 모니터링용 (§ 적체 감시): 아래 두 쿼리를 메트릭으로 노출할 것
--   SELECT status, count(*) FROM outbox_event GROUP BY status;          -- 적체량
--   SELECT now() - min(created_at) FROM outbox_event WHERE status LIKE '%PENDING';  -- 최악 지연
