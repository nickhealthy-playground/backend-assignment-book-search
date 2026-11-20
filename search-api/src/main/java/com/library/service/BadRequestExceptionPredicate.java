package com.library.service;

import com.library.ApiException;
import org.springframework.http.HttpStatus;

import java.util.function.Predicate;

/**
 * 외부 API가 400(요청 값 자체가 잘못됨)을 응답한 경우를 판별한다.
 * 400은 외부 API 장애가 아니고 다른 API로 보내도 결과가 같으므로 서킷 실패 집계와 fallback 대상에서 제외한다.
 * 401·403·429는 키·쿼터 문제로 해당 API를 쓸 수 없는 상태라 장애로 집계하고 fallback 한다.
 *
 * resilience4j ignore-exception-predicate 설정에서 리플렉션으로 생성하므로 기본 생성자가 필요하다.
 */
public class BadRequestExceptionPredicate implements Predicate<Throwable> {
    @Override
    public boolean test(Throwable throwable) {
        return throwable instanceof ApiException e && e.getHttpStatus() == HttpStatus.BAD_REQUEST;
    }
}
