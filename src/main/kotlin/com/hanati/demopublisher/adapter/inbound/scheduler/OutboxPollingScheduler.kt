package com.hanati.demopublisher.adapter.inbound.scheduler

import com.hanati.demopublisher.application.port.inbound.RelayOutboxUseCase
import com.hanati.demopublisher.domain.RelayStage
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Driving Adapter (Inbound Port)
 * - 트리거 감지
 * - 프로토콜 -> 도메인 번역
 * - 인바운드 포트 호출
 */
@Component
class OutboxPollingScheduler(
    private val relayOutboxUseCase: RelayOutboxUseCase,
) {
    /**
     * 한 poll()에서 두 스테이지를 순차 실행.
     * fixedDelay가 poll() 전체에 걸리므로 스테이지 간에도 겹침이 없다.
     * fixedDelay = 이전 실행 "종료" 후 대기시간이므로 실행 겹침이 없다.
     */
    /**
     * 폴링 스케줄러
     * - RelayStage(MINT 관련)
     */
    @Scheduled(fixedDelayString = "\${outbox.polling.fixed-delay}")
    fun poll() {
        RelayStage.entries.forEach { relayOutboxUseCase.relay(it) }
    }
}
