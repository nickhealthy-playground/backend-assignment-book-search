package com.library.config

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AsyncConfigTest extends Specification {
    ThreadPoolTaskExecutor executor = new AsyncConfig().getAsyncExecutor() as ThreadPoolTaskExecutor
    CountDownLatch release = new CountDownLatch(1)

    void cleanup() {
        release.countDown()
        executor.shutdown()
    }

    def "스레드와 작업 큐가 모두 차도 작업이 거절되지 않고, 작업을 제출한 호출 스레드에서 직접 실행된다."() {
        given: "최대 스레드 수 + 큐 용량만큼 끝나지 않는 작업으로 executor를 포화시킨다."
        def capacity = executor.maxPoolSize + executor.threadPoolExecutor.queue.remainingCapacity()
        capacity.times { executor.execute { release.await(10, TimeUnit.SECONDS) } }

        when:
        Thread executedOn = null
        executor.execute { executedOn = Thread.currentThread() }

        then: "기본 AbortPolicy였다면 TaskRejectedException이 호출 스레드(검색 요청)로 전파된다."
        noExceptionThrown()
        executedOn == Thread.currentThread()
    }
}
