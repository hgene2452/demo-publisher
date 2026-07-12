package com.hanati.demopublisher.application.port.outbound

import com.hanati.demopublisher.domain.OutboxEvent

/**
 * Outbound Port: 메시지 브로커 발행.
 * 구현체는 브로커 ack 를 동기적으로 확인해야 한다 (at-least-once 보장의 전제).
 */
interface PublishEventPort {
    fun publish(event: OutboxEvent)
}
