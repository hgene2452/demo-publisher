package com.hanati.demopublisher.application.port.inbound

import com.hanati.demopublisher.domain.RelayStage

/**
 * Inbound Port: 아웃박스 릴레이 유스케이스.
 * 드라이빙 어댑터(스케줄러)가 이 포트를 통해 애플리케이션을 호출한다.
 */
interface RelayOutboxUseCase {
    /** 해당 스테이지의 대기 이벤트를 배치로 발행하고 상태를 전이시킨다. */
    fun relay(stage: RelayStage)
}
