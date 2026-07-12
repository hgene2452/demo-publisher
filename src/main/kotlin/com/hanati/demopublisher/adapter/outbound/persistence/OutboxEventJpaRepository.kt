package com.hanati.demopublisher.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface OutboxEventJpaRepository : JpaRepository<OutboxEventJpaEntity, Long> {

    /**
     * FOR UPDATE SKIP LOCKED:
     * - 이미 다른 트랜잭션(=다른 폴러 인스턴스)이 잠근 행은 건너뛴다.
     * - 잠금은 relay() 트랜잭션 커밋/롤백 시점에 해제된다.
     */
    @Query(
        value = """
            SELECT * FROM outbox_event
            WHERE status = 'PENDING'
            ORDER BY id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true,
    )
    fun findPendingForUpdate(@Param("limit") limit: Int): List<OutboxEventJpaEntity>

    @Modifying
    @Query(
        """
        UPDATE OutboxEventJpaEntity e
        SET e.status = com.hanati.demopublisher.domain.OutboxStatus.PUBLISHED,
            e.publishedAt = :now
        WHERE e.id = :id
        """,
    )
    fun markPublished(@Param("id") id: Long, @Param("now") now: Instant)

    @Modifying
    @Query(
        """
        UPDATE OutboxEventJpaEntity e
        SET e.retryCount = e.retryCount + 1,
            e.status = CASE
                WHEN e.retryCount + 1 >= :maxRetry
                THEN com.hanati.demopublisher.domain.OutboxStatus.FAILED
                ELSE com.hanati.demopublisher.domain.OutboxStatus.PENDING
            END
        WHERE e.id = :id
        """,
    )
    fun increaseRetryOrFail(@Param("id") id: Long, @Param("maxRetry") maxRetry: Int)
}
