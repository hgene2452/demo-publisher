# demo-publisher

Kafka Outbox Publisher (Polling Relay) — Hexagonal Architecture

## 구조

```
com.hanati.demopublisher
├── domain
│   └── OutboxEvent.kt                    # 도메인 모델 + 상태 enum
├── application
│   ├── port
│   │   ├── inbound
│   │   │   └── RelayOutboxUseCase.kt     # 유스케이스 (Inbound Port)
│   │   └── outbound
│   │       ├── LoadPendingOutboxEventsPort.kt
│   │       ├── UpdateOutboxEventPort.kt
│   │       └── PublishEventPort.kt
│   └── service
│       └── OutboxRelayService.kt         # 유스케이스 구현 (트랜잭션 경계)
└── adapter
    ├── inbound
    │   └── scheduler
    │       └── OutboxPollingScheduler.kt  # Driving Adapter (@Scheduled)
    └── outbound
        ├── persistence
        │   ├── OutboxEventJpaEntity.kt
        │   ├── OutboxEventJpaRepository.kt  # FOR UPDATE SKIP LOCKED
        │   └── OutboxEventPersistenceAdapter.kt
        └── kafka
            └── KafkaEventPublishAdapter.kt  # 동기 ack (at-least-once)
```

## 흐름

1. 비즈니스 트랜잭션이 `outbox_event`에 INSERT (이 모듈 범위 밖)
2. `OutboxPollingScheduler`가 1초마다 `relay()` 호출
3. `SELECT ... FOR UPDATE SKIP LOCKED`로 PENDING 배치 조회 (멀티 인스턴스 안전)
4. Kafka 발행 → 브로커 ack 동기 확인 → `PUBLISHED` 마킹
5. 실패 시 retry_count 증가, max-retry 초과 시 `FAILED`

## 실행

```bash
docker compose up -d
./gradlew bootRun
```

## 테스트용 이벤트 삽입

```sql
INSERT INTO outbox_event (aggregate_type, aggregate_id, event_type, payload)
VALUES ('Remittance', 'RMT-0001', 'RemittanceRequested',
        '{"remittanceId": "RMT-0001", "amount": 1000000, "currency": "KRW"}');
```

```bash
# 확인
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic outbox-events --from-beginning
```
