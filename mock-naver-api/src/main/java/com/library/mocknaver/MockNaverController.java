package com.library.mocknaver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 네이버 '검색 - 책' API(/v1/search/book.json)의 요청/응답 스펙을 재현합니다.
 * - 인증 헤더(X-Naver-Client-Id / X-Naver-Client-Secret): 값은 아무거나 가능하지만, 없으면 네이버와 동일하게 에러 응답
 * - 응답 필드: lastBuildDate, total, start, display, items[] (title, link, image, author, discount, publisher, pubdate, isbn, description)
 * - 에러 응답: { "errorMessage": ..., "errorCode": ... }
 */
@RestController
public class MockNaverController {
    private static final DateTimeFormatter LAST_BUILD_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    private final FailureSimulator failureSimulator;

    public MockNaverController(FailureSimulator failureSimulator) {
        this.failureSimulator = failureSimulator;
    }

    @GetMapping("/v1/search/book.json")
    public ResponseEntity<?> search(
            @RequestHeader(value = "X-Naver-Client-Id", required = false) String clientId,
            @RequestHeader(value = "X-Naver-Client-Secret", required = false) String clientSecret,
            @RequestParam("query") String query,
            @RequestParam(value = "start", defaultValue = "1") int start,
            @RequestParam(value = "display", defaultValue = "10") int display) {

        // 서킷브레이커 실습용 장애 시뮬레이션 (기본값: 비활성)
        ResponseEntity<?> failure = failureSimulator.simulate();
        if (failure != null) {
            return failure;
        }

        // 실제 네이버와 동일하게 인증 헤더가 없으면 401 에러 (값 검증은 하지 않음)
        if (isBlank(clientId) || isBlank(clientSecret)) {
            return error(HttpStatus.UNAUTHORIZED, "NID AUTH Result Invalid (1000) : Authentication failed.", "024");
        }
        if (isBlank(query)) {
            return error(HttpStatus.BAD_REQUEST, "query is missing.", "SE01");
        }
        if (start < 1 || start > 1000) {
            return error(HttpStatus.BAD_REQUEST, "Invalid start value (부적절한 start 값입니다.)", "SE04");
        }
        if (display < 1 || display > 100) {
            return error(HttpStatus.BAD_REQUEST, "Invalid display value (부적절한 display 값입니다.)", "SE03");
        }

        List<MockBookData.Book> matched = MockBookData.search(query);
        int fromIndex = Math.min(start - 1, matched.size());
        int toIndex = Math.min(fromIndex + display, matched.size());
        List<MockBookData.Book> pageItems = matched.subList(fromIndex, toIndex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("lastBuildDate", ZonedDateTime.now().format(LAST_BUILD_DATE_FORMAT));
        body.put("total", matched.size());
        body.put("start", start);
        body.put("display", pageItems.size());
        body.put("items", pageItems.stream().map(MockBookData.Book::toItemMap).toList());
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status)
                .body(Map.of("errorMessage", message, "errorCode", code));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
