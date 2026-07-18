package com.hanati.demopublisher.adapter.outbound.kafka

import com.hanati.demopublisher.application.port.outbound.PublishEventPort
import com.hanati.demopublisher.domain.OutboxEvent
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class KafkaEventPublishAdapter(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${outbox.topic}") private val topic: String,
    @Value("\${outbox.polling.publish-timeout-ms}") private val publishTimeoutMs: Long,
) : PublishEventPort {

    override fun publish(event: OutboxEvent) {
        val record = ProducerRecord(topic, event.aggregateId, event.payload).apply {
            headers().add("eventId", event.id.toString().toByteArray())
            headers().add("eventType", event.eventType.toByteArray())  // MintRequested / MintConfirmed
        }
        kafkaTemplate.send(record).get(publishTimeoutMs, TimeUnit.MILLISECONDS)
    }
}
