package com.library;

import com.library.entity.DailyStat;
import com.library.repository.DailyStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// 로컬에서 통계 API를 바로 확인하기 위한 샘플 데이터. 실제 통계와 섞이지 않도록 local에서만 적재한다
@Profile("local")
@RequiredArgsConstructor
@Component
public class ApplicationRunner implements CommandLineRunner {
    private final DailyStatRepository dailyStatRepository;

    @Override
    public void run(String... args) throws Exception {
        DailyStat stat1 = new DailyStat("HTTP", LocalDateTime.now());
        DailyStat stat2 = new DailyStat("HTTP", LocalDateTime.now());
        DailyStat stat3 = new DailyStat("HTTP", LocalDateTime.now());
        DailyStat stat4 = new DailyStat("HTTP", LocalDateTime.now());
        DailyStat stat5 = new DailyStat("HTTP", LocalDateTime.now());
        DailyStat stat6 = new DailyStat("HTTP", LocalDateTime.now());
        DailyStat stat7 = new DailyStat("HTTP", LocalDateTime.now());

        DailyStat stat8 = new DailyStat("JAVA", LocalDateTime.now());
        DailyStat stat9 = new DailyStat("JAVA", LocalDateTime.now());

        DailyStat stat10 = new DailyStat("Kotlin", LocalDateTime.now());

        DailyStat stat11 = new DailyStat("Databases", LocalDateTime.now());

        DailyStat stat12 = new DailyStat("OS", LocalDateTime.now());

        dailyStatRepository.saveAll(List.of(stat1, stat2, stat3, stat4, stat5, stat6, stat7, stat8, stat9, stat10, stat11, stat12));
    }
}
