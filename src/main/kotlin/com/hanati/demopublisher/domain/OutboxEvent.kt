package com.hanati.demopublisher.domain

import java.time.Instant

/**
 * 아웃박스 이벤트 도메인 모델.
 * 어댑터(JPA 엔티티)와 분리하여 도메인이 인프라에 의존하지 않도록 한다.
 */
data class OutboxEvent(
    val id: Long,
    val aggregateType: String,
    val aggregateId: String,
    val eventType: String,
    val payload: String,
    val status: OutboxStatus,
    val retryCount: Int,
    val createdAt: Instant,
    val publishedAt: Instant?,
)

enum class OutboxStatus {
    PENDING,    // 발행 대기
    PUBLISHED,  // Kafka 발행 완료
    FAILED,     // max-retry 초과, 수동 개입 필요
}
