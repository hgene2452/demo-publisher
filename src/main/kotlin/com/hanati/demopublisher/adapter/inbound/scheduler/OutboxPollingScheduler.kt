package com.hanati.demopublisher.adapter.inbound.scheduler

import com.hanati.demopublisher.application.port.inbound.RelayOutboxUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Driving Adapter: 폴링 스케줄러.
 * fixedDelay = 이전 실행 "종료" 후 대기시간이므로 실행 겹침이 없다.
 */
@Component
class OutboxPollingScheduler(
    private val relayOutboxUseCase: RelayOutboxUseCase,
) {
    @Scheduled(fixedDelayString = "\${outbox.polling.fixed-delay}")
    fun poll() {
        relayOutboxUseCase.relay()
    }
}
