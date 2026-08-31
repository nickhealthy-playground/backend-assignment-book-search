package com.library.mocknaver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 서킷브레이커 실습을 위한 장애 시뮬레이터.
 * 강의의 서킷브레이커 챕터에서 "네이버 API 장애" 상황을 재현할 때 사용합니다.
 *
 * 사용법 (터미널에서):
 *   에러 모드 켜기   : curl -X POST "http://localhost:9090/mock-admin/failure?mode=error"
 *   지연 모드 켜기   : curl -X POST "http://localhost:9090/mock-admin/failure?mode=delay&delayMs=5000"
 *   정상으로 복구    : curl -X POST "http://localhost:9090/mock-admin/failure?mode=none"
 *   현재 상태 확인   : curl "http://localhost:9090/mock-admin/status"
 *
 * (mock 서버 프로세스를 그냥 종료해도 Connection Refused 로 장애 상황이 재현됩니다.)
 */
@Component
public class FailureSimulator {
    public enum Mode { NONE, ERROR, DELAY }

    private volatile Mode mode = Mode.NONE;
    private volatile long delayMs = 3000;

    /** 장애 모드에 따라 에러 응답을 반환하고, 정상 모드면 null을 반환합니다. */
    public ResponseEntity<?> simulate() {
        if (mode == Mode.ERROR) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorMessage", "system error (시스템 에러 - Mock 장애 시뮬레이션)", "errorCode", "900"));
        }
        if (mode == Mode.DELAY) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    public void change(Mode mode, Long delayMs) {
        this.mode = mode;
        if (delayMs != null && delayMs > 0) {
            this.delayMs = delayMs;
        }
    }

    public Map<String, Object> status() {
        return Map.of("mode", mode.name(), "delayMs", delayMs);
    }
}
