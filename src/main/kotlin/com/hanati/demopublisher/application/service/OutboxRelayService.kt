package com.hanati.demopublisher.application.service

import com.hanati.demopublisher.application.port.inbound.RelayOutboxUseCase
import com.hanati.demopublisher.application.port.outbound.LoadPendingOutboxEventsPort
import com.hanati.demopublisher.application.port.outbound.PublishEventPort
import com.hanati.demopublisher.application.port.outbound.UpdateOutboxEventPort
import com.hanati.demopublisher.domain.RelayStage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OutboxRelayService(
    private val loadPendingOutboxEventsPort: LoadPendingOutboxEventsPort,
    private val updateOutboxEventPort: UpdateOutboxEventPort,
    private val publishEventPort: PublishEventPort,
    @Value("\${outbox.polling.batch-size}") private val batchSize: Int,
    @Value("\${outbox.polling.max-retry}") private val maxRetry: Int,
) : RelayOutboxUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 트랜잭션 경계 = SELECT ... FOR UPDATE SKIP LOCKED 의 잠금 유지 구간.
     * 이 트랜잭션 안에서 조회 → 발행 → 상태 갱신이 이뤄지므로
     * 다른 인스턴스는 같은 행을 건너뛴다(SKIP LOCKED). 별도 분산 락 불필요.
     *
     * 장애 시나리오:
     * - Kafka 발행 성공 후 커밋 실패 → 다음 폴링에서 재발행 (at-least-once).
     *   중복 제거는 컨슈머 Inbox(event_id 멱등성)가 담당.
     */
    @Transactional
    override fun relay(stage: RelayStage) {
        val events = loadPendingOutboxEventsPort.loadPendingEvents(stage.fromStatus, batchSize)
        if (events.isEmpty()) return

        var published = 0
        for (event in events) {
            try {
                // 발행 이벤트 타입은 스테이지가 결정 (MintRequested / MintConfirmed)
                publishEventPort.publish(event.copy(eventType = stage.publishEventType))
                updateOutboxEventPort.advanceStatus(event.id, stage.toStatus)
                published++
            } catch (e: Exception) {
                log.error("outbox publish failed. stage={}, id={}", stage, event.id, e)
                updateOutboxEventPort.markRetryOrFail(event.id, maxRetry)
            }
        }
        log.info("outbox relay done. stage={}, fetched={}, published={}", stage, events.size, published)
    }
}
