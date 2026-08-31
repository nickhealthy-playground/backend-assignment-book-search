package com.library.mocknaver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 네이버 '검색 - 책' API가 2026년 7월 31일부로 종료되어,
 * 강의 실습을 위해 네이버 응답 스펙을 그대로 재현한 Mock 서버입니다.
 *
 * 실행: ./gradlew :mock-naver-api:bootRun (기본 포트 9090)
 */
@SpringBootApplication
public class MockNaverApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MockNaverApiApplication.class, args);
    }
}
