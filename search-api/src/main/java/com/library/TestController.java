package com.library;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// 비동기 스레드풀 부하 확인용. 요청 1건이 자기 자신을 10번 호출하므로 local 외 환경에 노출하지 않는다
@Slf4j
@Profile("local")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/test")
public class TestController {
    private final RestClient restClient = RestClient.create();

    @GetMapping
    public void test() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int requestId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    ResponseEntity<String> result = restClient.get()
                            .uri("http://localhost:8080/v1/books?query=java&page=1&size=2")
                            .retrieve()
                            .toEntity(String.class);
                    log.info("result = {} requestId = {}", result, requestId);
                } catch (Exception e) {
                    log.error("실패!!! requestId={}", requestId, e);
                }
            });
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
    }

}
