package com.hanati.demopublisher.application.port.outbound

import com.hanati.demopublisher.domain.OutboxEvent

/**
 * Outbound Port: PENDING 이벤트 조회.
 * 구현체는 FOR UPDATE SKIP LOCKED 로 행 잠금을 걸어
 * 멀티 인스턴스 환경에서도 중복 폴링을 방지해야 한다.
 */
interface LoadPendingOutboxEventsPort {
    fun loadPendingEvents(limit: Int): List<OutboxEvent>
}
