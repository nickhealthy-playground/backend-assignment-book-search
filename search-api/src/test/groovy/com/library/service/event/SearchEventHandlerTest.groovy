package com.library.service.event

import com.library.entity.DailyStat
import com.library.service.DailyStatCommandService
import spock.lang.Specification

import java.time.LocalDateTime

class SearchEventHandlerTest extends Specification {
    def "handleEvent"() {
        given:
        def commandService = Mock(DailyStatCommandService)
        def eventHandler = new SearchEventHandler(commandService)
        def event = new SearchEvent("HTTP", LocalDateTime.now())

        when:
        eventHandler.handleEvent(event)

        then:
        1 * commandService.save(_ as DailyStat)
    }

    def "자정 직전에 발생한 검색 이벤트가 자정 이후에 처리되어도, 통계는 처리 시각이 아닌 이벤트 발생 시각으로 저장된다."() {
        given:
        def commandService = Mock(DailyStatCommandService)
        def eventHandler = new SearchEventHandler(commandService)
        def occurredAt = LocalDateTime.of(2026, 1, 1, 23, 59, 59)
        def event = new SearchEvent("HTTP", occurredAt)

        when:
        eventHandler.handleEvent(event)

        then:
        1 * commandService.save({ DailyStat stat ->
            stat.query == "HTTP" && stat.eventDateTime == occurredAt
        })
    }
}
