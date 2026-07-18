package com.hanati.demopublisher.application.service

import com.hanati.demopublisher.application.port.inbound.RelayOutboxUseCase
import com.hanati.demopublisher.domain.RelayStage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * fetch와 tx의 분리:
 * - batch-size = relay() 1회가 처리할 총량 상한 (처리량 하한 제약을 만족하도록)
 * - chunk-size = 트랜잭션 1개의 단위 (잠금 상한 제약을 만족하도록)
 *
 * relay()는 트랜잭션 없이 청크 처리를 반복 호출하는 역할만 수행한다.
 * 각 청크가 커밋되는 즉시 해당 행들의 잠금이 해제되므로,
 * Kafka 장애 시에도 잠기는 것은 "현재 처리 중인 청크"뿐이다.
 */
@Service
class OutboxRelayService(
    private val chunkProcessor: OutboxRelayChunkProcessor,
    @Value("\${outbox.polling.batch-size}") private val batchSize: Int,
    @Value("\${outbox.polling.chunk-size}") private val chunkSize: Int,
) : RelayOutboxUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun relay(stage: RelayStage) {
        var processed = 0
        while (processed < batchSize) {
            val size = minOf(chunkSize, batchSize - processed)
            val n = chunkProcessor.processChunk(stage, size)   // 청크마다 독립 tx
            if (n == 0) break                                   // 대기 이벤트 소진
            processed += n
        }
        if (processed > 0) {
            log.info("outbox relay done. stage={}, processed={}", stage, processed)
        }
    }
}
