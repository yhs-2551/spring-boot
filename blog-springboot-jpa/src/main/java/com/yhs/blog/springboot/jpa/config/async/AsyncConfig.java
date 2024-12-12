package com.yhs.blog.springboot.jpa.config.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 기본 스레드 수
        executor.setMaxPoolSize(10); // 최대 스레드 수
        executor.setQueueCapacity(10); // 대기 큐 사이즈
        executor.setThreadNamePrefix("Async-"); // 스레드 이름 접두사
        // 초과된 작업 처리 정책. 최대 스레드 수 및 큐까지 모두 차면 CallerRunsPolicy 정책에 의해 요청 쓰레드 (톰캣 쓰레드)에서 직접 해당 작업을 처리한다.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60); // 유휴 스레드 유지 시간(60초)
        executor.setAwaitTerminationSeconds(60); // 어플리케이션 종료 시 작업 완료 대기 시간. 즉 앱 종료시 실행중인 작업을 기다리는 시간(60초)
        executor.initialize();
        return executor;

    }

    // 비동기 작업 중 발생하는 예외처리 관련 메서드. 일단 주석 처리
//    @Override
//    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//
//    }
}
