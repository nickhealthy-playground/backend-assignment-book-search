package com.library.service.event;

import com.library.entity.DailyStat;
import com.library.service.DailyStatCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEventHandler {
    private final DailyStatCommandService dailyStatCommandService;

    @Async
    @EventListener
    public void handleEvent(SearchEvent event) throws InterruptedException {
        log.info("[SearchEventHandler] handleEvent: {}", event);
        // 비동기 큐 대기 중 자정을 넘기면 처리 시각 기준으로는 다음 날로 집계되므로, 이벤트 발생 시각을 저장한다
        DailyStat dailyStat = new DailyStat(event.query(), event.timestamp());
        dailyStatCommandService.save(dailyStat);
    }

}
