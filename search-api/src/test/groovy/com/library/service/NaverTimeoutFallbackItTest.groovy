package com.library.service

import com.library.controller.response.PageResult
import com.library.repository.KakaoBookRepository
import com.sun.net.httpserver.HttpServer
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

/**
 * 실제 Feign 클라이언트(NaverClient)가 응답이 느린 네이버 stub을 호출할 때
 * read timeout으로 끊기고 카카오로 fallback 되는지 검증한다.
 * stub은 별도 의존성 없이 JDK 내장 HttpServer를 사용한다.
 */
@ActiveProfiles("test")
@SpringBootTest
class NaverTimeoutFallbackItTest extends Specification {
    static final long NAVER_DELAY_MS = 5_000
    static final HttpServer slowNaver = startSlowNaver()

    @DynamicPropertySource
    static void naverUrl(DynamicPropertyRegistry registry) {
        registry.add("external.naver.url", { "http://localhost:${slowNaver.address.port}".toString() })
    }

    @Autowired
    BookQueryService bookQueryService

    @SpringBean
    KakaoBookRepository kakaoBookRepository = Mock()

    void cleanupSpec() {
        slowNaver.stop(0)
    }

    def "네이버가 정상 응답을 read timeout보다 늦게 주면, 기다리지 않고 타임아웃으로 끊은 뒤 카카오로 fallback 한다."() {
        given:
        def kakaoResponse = new PageResult(1, 10, 0, [])

        when:
        def startedAt = System.nanoTime()
        def result = bookQueryService.search("HTTP", 1, 10)
        def elapsedMs = (System.nanoTime() - startedAt).intdiv(1_000_000)

        then:
        1 * kakaoBookRepository.search("HTTP", 1, 10) >> kakaoResponse
        result == kakaoResponse

        and: "Feign 기본 read timeout(60초)이면 5초를 기다려 네이버의 정상 응답을 받게 된다."
        elapsedMs < NAVER_DELAY_MS
    }

    private static HttpServer startSlowNaver() {
        def server = HttpServer.create(new InetSocketAddress("localhost", 0), 0)
        server.executor = Executors.newCachedThreadPool()
        server.createContext("/v1/search/book.json") { exchange ->
            Thread.sleep(NAVER_DELAY_MS)
            def body = '{"lastBuildDate":"","total":0,"start":1,"display":0,"items":[]}'.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.length)
            exchange.responseBody.withCloseable { it.write(body) }
        }
        server.start()
        return server
    }
}
