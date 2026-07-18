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
    MINT_REQUEST_PENDING,   // 미발행 (신청 이벤트 발행 대기)
    MINT_REQUESTED,         // 발행 신청 완료 (외부에서 확정 조건 충족 시 CONFIRM_PENDING으로 전이)
    MINT_CONFIRM_PENDING,   // 발행 확정 이벤트 발행 대기
    MINT_COMPLETED,         // 발행 완료
    FAILED,                 // max-retry 초과, 수동 개입 필요
}

/**
 * 릴레이 스테이지 = (어떤 상태를 폴링 → 무슨 이벤트를 발행 → 어떤 상태로 전이)
 * 민트 시퀀스의 두 발행 단계를 선언적으로 정의한다.
 */
enum class RelayStage(
    val fromStatus: OutboxStatus,
    val toStatus: OutboxStatus,
    val publishEventType: String,
) {
    MINT_REQUEST(OutboxStatus.MINT_REQUEST_PENDING, OutboxStatus.MINT_REQUESTED, "MintRequested"),
    MINT_CONFIRM(OutboxStatus.MINT_CONFIRM_PENDING, OutboxStatus.MINT_COMPLETED, "MintConfirmed"),
}
