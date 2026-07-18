package com.hanati.demopublisher.application.service

import com.hanati.demopublisher.application.port.outbound.LoadPendingOutboxEventsPort
import com.hanati.demopublisher.application.port.outbound.PublishEventPort
import com.hanati.demopublisher.application.port.outbound.UpdateOutboxEventPort
import com.hanati.demopublisher.domain.RelayStage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 청크 1개 = 트랜잭션 1개 = 잠금 구간 1개.
 *
 * 잠금 유지 시간이 "배치 전체"가 아니라 "청크 1개의 발행 시간"으로 축소된다.
 * 최악 잠금 시간 = chunk-size × publish-timeout ≤ 잠금 예산.
 *
 * 주의: OutboxRelayService와 별도 빈으로 분리한 이유 —
 * @Transactional은 프록시 기반이라 같은 클래스 내 자기 호출(self-invocation)에는 적용되지 않는다.
 */
@Component
class OutboxRelayChunkProcessor(
    private val loadPendingOutboxEventsPort: LoadPendingOutboxEventsPort,
    private val updateOutboxEventPort: UpdateOutboxEventPort,
    private val publishEventPort: PublishEventPort,
    @Value("\${outbox.polling.max-retry}") private val maxRetry: Int,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * @return 이번 청크에서 조회된 건수 (0이면 해당 스테이지에 더 이상 대기 없음)
     */
    @Transactional
    fun processChunk(stage: RelayStage, chunkSize: Int): Int {
        val events = loadPendingOutboxEventsPort.loadPendingEvents(stage.fromStatus, chunkSize)
        if (events.isEmpty()) return 0

        for (event in events) {
            try {
                publishEventPort.publish(event.copy(eventType = stage.publishEventType))
                updateOutboxEventPort.advanceStatus(event.id, stage.toStatus)
            } catch (e: Exception) {
                log.error("outbox publish failed. stage={}, id={}", stage, event.id, e)
                updateOutboxEventPort.markRetryOrFail(event.id, maxRetry)
            }
        }
        return events.size
    }
}
