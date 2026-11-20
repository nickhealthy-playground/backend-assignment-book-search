package com.library.service

import com.library.ApiException
import com.library.ErrorType
import com.library.controller.response.PageResult
import com.library.controller.response.SearchResponse
import com.library.repository.KakaoBookRepository
import com.library.repository.NaverBookRepository
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.Duration

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@SpringBootTest
class BookQueryServiceItTest extends Specification {
    @Autowired
    BookQueryService bookQueryService

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry

    @SpringBean
    KakaoBookRepository kakaoBookRepository = Mock()

    @SpringBean
    NaverBookRepository naverBookRepository = Mock()

    def "정상상황에서는 Circuit의 상태가 CLOSED이고, naver쪽으로 호출이 들어간다."() {
        given:
        def keyword = 'HTTP'
        def page = 1
        def size = 10

        when:
        bookQueryService.search(keyword, page, size)

        then:
        1 * naverBookRepository.search(keyword, page, size) >> new PageResult<>(1, 10, 0, [])

        and:
        def circuitBreaker = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .findFirst()
                .get()
        circuitBreaker.state == CircuitBreaker.State.CLOSED

        and:
        0 * kakaoBookRepository.search(*_)
    }

    def "circuit이 open되면 kakao쪽으로 호출이 들어간다."() {
        given:
        def keyword = 'HTTP'
        def page = 1
        def size = 10
        def kakaoResponse = new PageResult(1, 10, 0, [])

        def config = CircuitBreakerConfig.custom()
                .slidingWindowSize(1)
                .minimumNumberOfCalls(1)
                .failureRateThreshold(50)
                .build()
        circuitBreakerRegistry.circuitBreaker("naverSearch", config)

        and: "naver쪽은 항상 예외가 발생한다."
        naverBookRepository.search(keyword, page, size) >> { throw new RuntimeException("error!") }

        when:
        def result = bookQueryService.search(keyword, page, size)

        then: "kakao쪽으로 FallBack 된다."
        1 * kakaoBookRepository.search(keyword, page, size) >> kakaoResponse

        and: "circuit이 OPEN된다."
        def circuitBreaker = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .findFirst()
                .get()
        circuitBreaker.state == CircuitBreaker.State.OPEN

        and:
        result == kakaoResponse
    }

    def "application.yml의 서킷브레이커 설정이 기본 설정으로 바인딩된다."() {
        when:
        def config = circuitBreakerRegistry.getDefaultConfig()

        then:
        verifyAll {
            config.slidingWindowSize == 100
            config.minimumNumberOfCalls == 10
            config.failureRateThreshold == 50f
            config.permittedNumberOfCallsInHalfOpenState == 10
            config.waitIntervalFunctionInOpenState.apply(1) == Duration.ofSeconds(20).toMillis()
            config.slowCallDurationThreshold == Duration.ofSeconds(2)
            config.slowCallRateThreshold == 50f
        }
    }

    def "네이버가 400(잘못된 요청)을 응답하면 카카오로 fallback 하지 않고 그대로 전달하며, 서킷 실패로도 집계하지 않는다."() {
        given: "yml 기본 설정을 유지한 채 1건만 실패해도 OPEN 되도록 윈도우만 줄인다."
        def circuitBreaker = createNaverCircuitBreakerOpeningOnFirstFailure()

        and:
        naverBookRepository.search(*_) >> {
            throw new ApiException("Invalid start value", ErrorType.EXTERNAL_API_ERROR, HttpStatus.BAD_REQUEST)
        }

        when:
        bookQueryService.search('HTTP', 1, 10)

        then:
        def e = thrown(ApiException)
        e.httpStatus == HttpStatus.BAD_REQUEST

        and:
        0 * kakaoBookRepository.search(*_)

        and:
        circuitBreaker.state == CircuitBreaker.State.CLOSED
        circuitBreaker.metrics.numberOfFailedCalls == 0
    }

    def "네이버가 #status 를 응답하면 네이버를 쓸 수 없는 상태로 보고 장애로 집계하며 카카오로 fallback 한다."() {
        given:
        def circuitBreaker = createNaverCircuitBreakerOpeningOnFirstFailure()
        def kakaoResponse = new PageResult(1, 10, 0, [])

        and:
        naverBookRepository.search(*_) >> {
            throw new ApiException("error", ErrorType.EXTERNAL_API_ERROR, status)
        }

        when:
        def result = bookQueryService.search('HTTP', 1, 10)

        then:
        1 * kakaoBookRepository.search('HTTP', 1, 10) >> kakaoResponse
        result == kakaoResponse

        and:
        circuitBreaker.state == CircuitBreaker.State.OPEN
        circuitBreaker.metrics.numberOfFailedCalls == 1

        where:
        status << [HttpStatus.UNAUTHORIZED, HttpStatus.TOO_MANY_REQUESTS, HttpStatus.INTERNAL_SERVER_ERROR]
    }

    private CircuitBreaker createNaverCircuitBreakerOpeningOnFirstFailure() {
        def config = CircuitBreakerConfig.from(circuitBreakerRegistry.getDefaultConfig())
                .slidingWindowSize(1)
                .minimumNumberOfCalls(1)
                .build()
        return circuitBreakerRegistry.circuitBreaker("naverSearch", config)
    }
}
