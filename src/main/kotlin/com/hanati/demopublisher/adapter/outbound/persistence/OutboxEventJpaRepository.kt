package com.hanati.demopublisher.adapter.outbound.persistence

import com.hanati.demopublisher.domain.OutboxStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface OutboxEventJpaRepository : JpaRepository<OutboxEventJpaEntity, Long> {

    @Query(
        value = """
            SELECT * FROM outbox_event
            WHERE status = :status
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true,
    )
    fun findPendingForUpdate(@Param("status") status: String, @Param("limit") limit: Int): List<OutboxEventJpaEntity>

    @Modifying
    @Query(
        """
        UPDATE OutboxEventJpaEntity e
        SET e.status = :toStatus, e.publishedAt = :now
        WHERE e.id = :id
        """,
    )
    fun advanceStatus(@Param("id") id: Long, @Param("toStatus") toStatus: OutboxStatus, @Param("now") now: Instant)

    @Modifying
    @Query(
        """
        UPDATE OutboxEventJpaEntity e
        SET e.retryCount = e.retryCount + 1,
            e.status = CASE
                WHEN e.retryCount + 1 >= :maxRetry
                THEN com.hanati.demopublisher.domain.OutboxStatus.FAILED
                ELSE e.status
            END
        WHERE e.id = :id
        """,
    )
    fun increaseRetryOrFail(@Param("id") id: Long, @Param("maxRetry") maxRetry: Int)
}
