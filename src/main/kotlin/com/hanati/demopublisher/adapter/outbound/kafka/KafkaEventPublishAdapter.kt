package com.hanati.demopublisher.adapter.outbound.kafka

import com.hanati.demopublisher.application.port.outbound.PublishEventPort
import com.hanati.demopublisher.domain.OutboxEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * Driven Adapter: Kafka 발행 어댑터.
 *
 * - key = aggregateId → 같은 애그리게잇의 이벤트는 같은 파티션 → 파티션 내 순서 보장
 * - .get(timeout) 으로 브로커 ack 를 동기 확인.
 *   ack 없이 markPublished 하면 유실 가능성이 생기므로 반드시 동기 대기.
 */
@Component
class KafkaEventPublishAdapter(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${outbox.topic}") private val topic: String,
    @Value("\${outbox.polling.publish-timeout-ms}") private val publishTimeoutMs: Long,
) : PublishEventPort {

    override fun publish(event: OutboxEvent) {
        kafkaTemplate
            .send(topic, event.aggregateId, event.payload)
            .get(publishTimeoutMs, TimeUnit.MILLISECONDS)
    }
}
