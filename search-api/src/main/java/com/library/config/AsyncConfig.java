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
        executor.initialize();
        return executor;
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
