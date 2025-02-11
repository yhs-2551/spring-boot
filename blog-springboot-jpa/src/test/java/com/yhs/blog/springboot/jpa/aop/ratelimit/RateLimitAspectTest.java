package com.yhs.blog.springboot.jpa.aop.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.common.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private ServletRequestAttributes attributes;

    private static final String TEST_IP = "127.0.0.1";
    private static final String TEST_KEY = "verifyCode";
    private static final String REDIS_KEY = "rateLimit" + TEST_KEY + ":" + TEST_IP;
    private static final String FIRST_REDIS_KEY = "firstRateLimit" + TEST_KEY + ":" + TEST_IP;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Nested
    @DisplayName("횟수 제한 및 로그인 실패시 Authentication 예외 발생 테스트 관련 내부 클래스")
    class RateLimitTest {

        @BeforeEach
        void setUp() {
            when(attributes.getRequest()).thenReturn(request);
            RequestContextHolder.setRequestAttributes(attributes);

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
            when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(TEST_IP);

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(rateLimit.key()).thenReturn(TEST_KEY);

        }

        @Test
        @DisplayName("첫 요청 테스트 및 요청을 실패했을때 횟수 관련 캐시가 새롭게 생성되는지 확인한다.")
        void 첫_요청_실패시_횟수제한_캐시_생성_테스트() throws Throwable {
            // given

            when(valueOperations.get(REDIS_KEY)).thenReturn(null);
            when(rateLimit.windowMinutes()).thenReturn(1L);
            RateLimitResponse<Void> errorResponse = new RateLimitResponse<>(false, "실패", 400, null);
            when(joinPoint.proceed()).thenReturn(errorResponse);

            // when
            Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimit);

            // then
            verify(valueOperations).set(eq(REDIS_KEY), eq("1"), eq(1L), eq(TimeUnit.MINUTES));
            assertThat(result).isEqualTo(errorResponse);
        }

        @Test
        @DisplayName("최대 시도 횟수를 초과 했을때 새롭게 만료시간이 설정되는지 테스트한다.")
        void 최대_시도횟수_초과시_만료시간_갱신_테스트() throws Throwable {

            // given

            when(valueOperations.get(REDIS_KEY)).thenReturn("3");
            when(rateLimit.maxAttempts()).thenReturn(3);
            when(rateLimit.windowMinutes()).thenReturn(1L);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                rateLimitAspect.checkRateLimit(joinPoint, rateLimit);
            });

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED);
            assertThat(exception.getMessage()).contains("너무 많은 시도입니다");
            verify(redisTemplate).expire(eq(REDIS_KEY), eq(1L), eq(TimeUnit.MINUTES));
            verify(joinPoint, never()).proceed();

        }

        @Test
        @DisplayName("최대 시도 횟수 초과 시 초기에만 제한 설정이 된다")
        void 최대시도_횟수_초과시_처음에만_제한설정() throws Throwable {

            // given

            when(valueOperations.get(REDIS_KEY)).thenReturn("3");
            when(rateLimit.maxAttempts()).thenReturn(3);
            when(rateLimit.windowMinutes()).thenReturn(1L);
            when(valueOperations.get(FIRST_REDIS_KEY)).thenReturn(null);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                rateLimitAspect.checkRateLimit(joinPoint, rateLimit);
            });

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED);
            assertThat(exception.getMessage()).contains("너무 많은 시도입니다");
            verify(valueOperations).set(eq(FIRST_REDIS_KEY), eq("true"));
            verify(redisTemplate).expire(eq(REDIS_KEY), eq(1L), eq(TimeUnit.MINUTES));
            verify(redisTemplate).expire(eq(FIRST_REDIS_KEY), eq(1L), eq(TimeUnit.MINUTES));

            // 두번째 시도, 두번째 시도 전에 아래 설정만 새롭게 업데이트 하고 기존 설정은 유지함. 즉 두 번째 시도 전에
            // FIRST_REDIS_KEY의 반환값만 변경

            when(valueOperations.get(FIRST_REDIS_KEY)).thenReturn("true");

            // when & then
            BusinessException exception2 = assertThrows(BusinessException.class, () -> {
                rateLimitAspect.checkRateLimit(joinPoint, rateLimit);
            });

            assertThat(exception2.getMessage()).contains("너무 많은 시도입니다");

            verify(redisTemplate, times(1)).expire(eq(REDIS_KEY), eq(1L), eq(TimeUnit.MINUTES));

        }

        @Test
        @DisplayName("응답에 성공하면 대상 메서드 실행, 횟수 1회 증가가 되는지 확인한다")
        void 응답_성공_테스트() throws Throwable {
            // given

            when(valueOperations.get(REDIS_KEY)).thenReturn("1");
            when(rateLimit.maxAttempts()).thenReturn(3);
            RateLimitResponse<Void> successResponse = new RateLimitResponse<>(true, "성공", 200, null);
            when(joinPoint.proceed()).thenReturn(successResponse);

            // when
            Object result = rateLimitAspect.checkRateLimit(joinPoint, rateLimit);

            // then
            assertThat(result).isEqualTo(successResponse);
            verify(valueOperations, times(1)).increment(REDIS_KEY); // increment 한번 호출됨
            verify(joinPoint, times(1)).proceed(); // proceed 한번 호출됨
        }

        @Test
        @DisplayName("로그인 실패시 횟수 증가 및 Authentication 예외 발생 확인")
        void 로그인_실패시_횟수증가_및_Authentication예외_발생_테스트() throws Throwable {
            // given

            when(valueOperations.get(REDIS_KEY)).thenReturn("1");

            when(rateLimit.maxAttempts()).thenReturn(3);

            when(joinPoint.proceed()).thenThrow(new AuthenticationException("로그인 실패") {
            });

            // when & then
            assertThrows(AuthenticationException.class, () -> {
                rateLimitAspect.checkRateLimit(joinPoint, rateLimit);
            });

            // 횟수 증가 검증
            verify(valueOperations).increment(anyString());
        }

    }

    @Nested
    @DisplayName("ClientIp 추출 관련 내부 클래스")
    class ClientIpTest {

        @Test
        @DisplayName("getClientIp 함수가 제대로 호출되어 Ip 주소가 추출 되는지 테스트")
        void 클라이언트_아이피_추출_테스트() throws Throwable {
            // given

            when(attributes.getRequest()).thenReturn(request);
            RequestContextHolder.setRequestAttributes(attributes);

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(rateLimit.key()).thenReturn(TEST_KEY);

            when(request.getHeader("X-Forwarded-For")).thenReturn("proxy-ip");
            when(joinPoint.proceed()).thenReturn(new RateLimitResponse<>(true, "성공", 200, null));

            // when
            rateLimitAspect.checkRateLimit(joinPoint, rateLimit);

            // then
            verify(request).getHeader("X-Forwarded-For"); // 함수가 실행됐는지 증명
        }

        @Test
        @DisplayName("RequestContextHolder.getRequestAttributes()값이_null일때 SystemException을 반환하는지 테스트")
        void RequestContextHolder_getRequestAttributes_값이_null일때_테스트() throws Throwable {
            // given
            RequestContextHolder.resetRequestAttributes(); // RequestContextHolder null로 설정

            // when & then
            assertThrows(SystemException.class, () -> {
                rateLimitAspect.checkRateLimit(joinPoint, rateLimit);
            });

        }
    }
}