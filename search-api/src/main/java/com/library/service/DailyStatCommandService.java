package com.library.service;

import com.library.entity.DailyStat;
import com.library.repository.DailyStatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStatCommandService {
    private final DailyStatRepository dailyStatRepository;

    @Transactional
    public void save(DailyStat dailyStat){
        log.info("save daily stat: {}", dailyStat);
        dailyStatRepository.save(dailyStat);
    }
}
