package com.library.service;

import com.library.ApiException;
import com.library.controller.response.PageResult;
import com.library.controller.response.SearchResponse;
import com.library.repository.BookRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookQueryService {
    private static final Predicate<Throwable> IS_BAD_REQUEST = new BadRequestExceptionPredicate();

    @Qualifier("naverBookRepository")
    private final BookRepository naverBookRepository;

    @Qualifier("kakaoBookRepository")
    private final BookRepository kakaoBookRepository;

    @CircuitBreaker(name = "naverSearch", fallbackMethod = "searchFallBack")
    public PageResult<SearchResponse> search(String query, int page, int size) {
        log.info("[BookQueryService] naver query = {}, page = {}, size = {}", query, page, size);
        return naverBookRepository.search(query, page, size);
    }

    public PageResult<SearchResponse> searchFallBack(String query, int page, int size, Throwable throwable) {
        // 서킷 집계에서 제외(ignore-exception-predicate)된 예외도 fallback 메서드는 호출되므로 여기서 한 번 더 걸러낸다
        if (IS_BAD_REQUEST.test(throwable)) {
            throw (ApiException) throwable;
        }
        if (throwable instanceof CallNotPermittedException) {
            return handleOpenCircuit(query, page, size);
        }
        return handleException(query, page, size, throwable);
    }

    private PageResult<SearchResponse> handleOpenCircuit(String query, int page, int size) {
        log.warn("[BookQueryService] Circuit Breaker is open! Fallback to kakao search.");
        return kakaoBookRepository.search(query, page, size);
    }

    private PageResult<SearchResponse> handleException(String query, int page, int size, Throwable throwable) {
        log.error("[BookQueryService] An error occurred! FallBack to kakao search. errorMessage={}", throwable.getMessage());
        return kakaoBookRepository.search(query, page, size);
    }

}
