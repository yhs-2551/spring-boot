package com.yhs.blog.springboot.jpa.aop.ratelimit;

import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RateLimit rateLimit;

    private static final String TEST_IP = "127.0.0.1";
    private static final String TEST_KEY = "test";
    private static final String REDIS_KEY = TEST_KEY + ":" + TEST_IP;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(rateLimit.key()).thenReturn(TEST_KEY);
//        when(rateLimit.maxAttempts()).thenReturn(3);

    }

    @Test
    @DisplayName("첫 요청 테스트 및 요청을 실패했을때 캐시가 새롭게 생성되는지 확인한다.")
    void firstRequestTest() throws Throwable {
        // given
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(rateLimit.windowMinutes()).thenReturn(1L);
        RateLimitResponse errorResponse = new RateLimitResponse(false, "실패", 400, null);
        when(joinPoint.proceed()).thenReturn(errorResponse);

        // when
        Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimit);

        // then
        verify(valueOperations).set(eq(REDIS_KEY), eq("1"), eq(1L), eq(TimeUnit.MINUTES));
        assertThat(result).isEqualTo(errorResponse);
    }

    @Test
    @DisplayName("최대 시도 횟수를 초과 했을때 새롭게 만료시간이 설정되는지 테스트한다.")
    void exceedMaxAttemptsTest() throws Throwable {

        // given
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        when(valueOperations.get(REDIS_KEY)).thenReturn("3");
        when(rateLimit.maxAttempts()).thenReturn(3);
        when(rateLimit.windowMinutes()).thenReturn(1L);


        // when
        RateLimitResponse response = (RateLimitResponse) rateLimitAspect.checkRateLimit(joinPoint, rateLimit);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("너무 많은 시도입니다");
        verify(redisTemplate).expire(eq(REDIS_KEY), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("응답에 성공하면 횟수 카운트를 증가시키지 않고 바로 성공 응답 리턴하는지 확인한다")
    void successResponseTest() throws Throwable {
        // given
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        when(valueOperations.get(REDIS_KEY)).thenReturn("1");
        when(rateLimit.maxAttempts()).thenReturn(3);
        RateLimitResponse successResponse = new RateLimitResponse(true, "성공", 200, null);
        when(joinPoint.proceed()).thenReturn(successResponse);

        // when
        Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimit);

        // then
        assertThat(result).isEqualTo(successResponse);
        verify(valueOperations, never()).increment(any());
    }

    @Test
    @DisplayName("getClientIp 함수가 제대로 호출되어 Ip 주소가 추출 되는지 테스트")
    void getClientIpTest() throws Throwable {
        // given
        when(request.getHeader("X-Forwarded-For")).thenReturn("proxy-ip");
        when(joinPoint.proceed()).thenReturn(new RateLimitResponse(true, "성공", 200, null));

        // when
        rateLimitAspect.checkRateLimit(joinPoint, rateLimit);

        // then
        verify(request).getHeader("X-Forwarded-For"); // 함수가 실행됐는지 증명
    }
}