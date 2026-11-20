package com.library.config;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean("lsExecutor")
    @Override
    public @Nullable Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cpuCoreCount = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cpuCoreCount); // 기본 스레드 풀 사이즈
        executor.setMaxPoolSize(cpuCoreCount * 2); // 맥스 스레드 풀 사이즈
        executor.setQueueCapacity(10); // 작업큐 용량 설정
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setThreadNamePrefix("LS-");
        executor.setRejectedExecutionHandler(new LoggingCallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    // 기본 AbortPolicy는 포화 시 TaskRejectedException을 이벤트 발행 스레드(검색 요청)로 던져, 검색은 성공했는데 500을 응답하게 만든다.
    // 통계를 버리지도(DiscardPolicy), 검색을 실패시키지도 않도록 호출 스레드에서 직접 실행한다. 대가로 포화 구간에는 검색 응답이 느려진다
    private static class LoggingCallerRunsPolicy extends ThreadPoolExecutor.CallerRunsPolicy {
        @Override
        public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
            log.warn("[AsyncConfig] 비동기 작업 큐 포화 - 호출 스레드에서 직접 실행합니다. poolSize={}, queueSize={}",
                    executor.getPoolSize(), executor.getQueue().size());
            super.rejectedExecution(task, executor);
        }
    }

    @Override
    public @Nullable AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    private class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable ex, Method method, @Nullable Object... params) {
            log.error("Failed to execute {}", ex.getMessage(), ex);
            Arrays.asList(params).forEach(param -> log.error("parameter value = {}", param));
        }
    }
}
