package com.hanati.demopublisher.adapter.outbound.persistence

import com.hanati.demopublisher.application.port.outbound.LoadPendingOutboxEventsPort
import com.hanati.demopublisher.application.port.outbound.UpdateOutboxEventPort
import com.hanati.demopublisher.domain.OutboxEvent
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Driven Adapter: JPA 기반 아웃박스 영속성 어댑터.
 */
@Component
class OutboxEventPersistenceAdapter(
    private val repository: OutboxEventJpaRepository,
) : LoadPendingOutboxEventsPort, UpdateOutboxEventPort {

    override fun loadPendingEvents(limit: Int): List<OutboxEvent> =
        repository.findPendingForUpdate(limit).map { it.toDomain() }

    override fun markPublished(id: Long) {
        repository.markPublished(id, Instant.now())
    }

    override fun markRetryOrFail(id: Long, maxRetry: Int) {
        repository.increaseRetryOrFail(id, maxRetry)
    }
}
