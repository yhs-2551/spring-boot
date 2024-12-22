package com.yhs.blog.springboot.jpa.config.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.yhs.blog.springboot.jpa.exception.custom.ElasticsearchCustomException;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableRetry
@Log4j2
// @EnableRetry @Retryable과 함께 사용하는 경우에 사용. 나의 경우 스프링 Bean 방식 사용
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 기본 스레드 수
        executor.setMaxPoolSize(10); // 최대 스레드 수
        executor.setQueueCapacity(10); // 대기 큐 사이즈
        executor.setThreadNamePrefix("Async-"); // 스레드 이름 접두사
        // 초과된 작업 처리 정책. 최대 스레드 수 및 큐까지 모두 차면 CallerRunsPolicy 정책에 의해 요청 쓰레드 (톰캣 쓰레드)에서
        // 직접 해당 작업을 처리한다.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60); // 유휴 스레드 유지 시간(60초)
        executor.setAwaitTerminationSeconds(60); // 어플리케이션 종료 시 작업 완료 대기 시간. 즉 앱 종료시 실행중인 작업을 기다리는 시간(60초)
        executor.initialize();
        return executor;

    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000L); // 1초 대기

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // 최대 3번 재시도

        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;

    }

    // 비동기 작업 중 발생하는 예외처리 관련 메서드.
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("비동기 함수 {} 에러: {}", method.getName(), ex);
            log.error("파라미터: {}", Arrays.toString(params));

            // Elasticsearch 관련 예외 처리
            if (ex instanceof ElasticsearchCustomException esEx) {

                // ElasticsearchCustomException esEx = (ElasticsearchCustomException) ex;
                log.error(
                        "Elasticsearch 오류 발생 - 메시지: {}, 코드: {}, 원인: {}",
                        esEx.getMessage(),
                        esEx.getErrorCode(),
                        Optional.ofNullable(esEx.getCause())
                                .map(Throwable::getMessage)
                                .orElse("원인 불명"));
                // 추가 에러 처리 로직 (모니터링 알림 등)
            }
        };
    }
    
}
