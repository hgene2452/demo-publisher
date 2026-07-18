package com.hanati.demopublisher.application.port.outbound

import com.hanati.demopublisher.domain.OutboxStatus

/**
 * Outbound Port: 아웃박스 이벤트 상태 갱신.
 */
interface UpdateOutboxEventPort {
    /** 발행 성공 시 다음 상태로 전이 */
    fun advanceStatus(id: Long, toStatus: OutboxStatus)

    /** retry_count 증가. maxRetry 도달 시 FAILED, 아니면 현재 상태 유지(재시도 대상으로 남김) */
    fun markRetryOrFail(id: Long, maxRetry: Int)
}
