package com.hanati.demopublisher.adapter.outbound.persistence

import com.hanati.demopublisher.application.port.outbound.LoadPendingOutboxEventsPort
import com.hanati.demopublisher.application.port.outbound.UpdateOutboxEventPort
import com.hanati.demopublisher.domain.OutboxEvent
import com.hanati.demopublisher.domain.OutboxStatus
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OutboxEventPersistenceAdapter(
    private val repository: OutboxEventJpaRepository,
) : LoadPendingOutboxEventsPort, UpdateOutboxEventPort {

    override fun loadPendingEvents(status: OutboxStatus, limit: Int): List<OutboxEvent> =
        repository.findPendingForUpdate(status.name, limit).map { it.toDomain() }

    override fun advanceStatus(id: Long, toStatus: OutboxStatus) {
        repository.advanceStatus(id, toStatus, Instant.now())
    }

    override fun markRetryOrFail(id: Long, maxRetry: Int) {
        repository.increaseRetryOrFail(id, maxRetry)
    }
}
