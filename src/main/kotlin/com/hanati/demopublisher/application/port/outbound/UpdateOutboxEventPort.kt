package com.hanati.demopublisher.application.port.outbound

/**
 * Outbound Port: 아웃박스 이벤트 상태 갱신.
 */
interface UpdateOutboxEventPort {
    fun markPublished(id: Long)

    /** retry_count 증가. maxRetry 도달 시 FAILED로 전환. */
    fun markRetryOrFail(id: Long, maxRetry: Int)
}
