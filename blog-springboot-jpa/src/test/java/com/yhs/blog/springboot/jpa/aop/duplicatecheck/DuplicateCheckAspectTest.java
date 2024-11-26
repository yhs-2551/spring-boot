package com.yhs.blog.springboot.jpa.aop.duplicatecheck;

import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
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

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateCheckAspectTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private DuplicateCheckAspect duplicateCheckAspect;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("중복 체크 시도 횟수 제한 테스트")
    class AttemptLimitTest {

        @Test
        @DisplayName("최대 시도 횟수를 초과하면 접근이 차단된다")
        void whenExceedMaxAttempts_thenBlockAccess() throws Throwable {
            // Given
            String clientIp = "127.0.0.1";
            String attemptKey = "duplicateCheck:" + clientIp;
            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(attemptKey)).thenReturn("3");

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint);

            // Then
            assertThat(response.isExist()).isFalse();
            assertThat(response.getMessage()).contains("너무 많은 시도입니다");
            assertThat(response.isLimited()).isTrue();
            verify(redisTemplate).expire(attemptKey, 1, TimeUnit.MINUTES);
            verify(joinPoint, never()).proceed();
        }

        @Test
        @DisplayName("첫 시도시 존재하지 않는 경우, 캐시가 생성된다")
        void whenFirstAttemptAndNotExists_thenCreateCache() throws Throwable {
            // Given
            String clientIp = "127.0.0.1";
            String attemptKey = "duplicateCheck:" + clientIp;
            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(attemptKey)).thenReturn(null);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "없음", false));

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint);

            // Then
            assertThat(response.isExist()).isFalse();
            assertThat(response.getMessage()).isEqualTo("없음");
            assertThat(response.isLimited()).isFalse();
            verify(valueOperations).set(attemptKey, "1", 1L, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("첫 시도가 아니고 존재하지 않는 경우, 기존의 캐시값이 증가한다")
        void whenNotFirstAttemptAndNotExists_thenIncreaseCache() throws Throwable {
            // Given
            String clientIp = "127.0.0.1";
            String attemptKey = "duplicateCheck:" + clientIp;
            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(attemptKey)).thenReturn("2");
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "없음", false));

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint);

            // Then
            assertThat(response.isExist()).isFalse();
            assertThat(response.getMessage()).isEqualTo("없음");
            assertThat(response.isLimited()).isFalse();
            verify(valueOperations).increment(attemptKey);
        }

        @Test
        @DisplayName("첫 시도시 존재하는 경우 캐시가 생성된다")
        void whenFirstAttemptAndExists_thenCreateCache() throws Throwable {
            // Given
            String clientIp = "127.0.0.1";
            String attemptKey = "duplicateCheck:" + clientIp;
            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(attemptKey)).thenReturn(null);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(true, "존재함", false));

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint);

            // Then
            assertThat(response.isExist()).isTrue();
            verify(valueOperations).set(attemptKey, "1", 1L, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("첫 시도가 아니고, 존재하는 경우 캐시가 증가한다")
        void whenNotFirstAttemptAndExists_thenIncreaseCache() throws Throwable {
            // Given
            String clientIp = "127.0.0.1";
            String attemptKey = "duplicateCheck:" + clientIp;
            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(attemptKey)).thenReturn("2");
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(true, "존재함", false));

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint);

            // Then
            assertThat(response.isExist()).isTrue();
            verify(valueOperations).increment(attemptKey);
        }

    }

    @Nested
    @DisplayName("클라이언트 IP 추출 테스트")
    class ClientIpTest {

        @Test
        @DisplayName("X-Forwarded-For 헤더가 있으면 해당 IP를 반환한다")
        void whenXForwardedForExists_thenReturnThatIp() throws Throwable {
            // Given
            String expectedIp = "192.168.1.1";
            when(request.getHeader("X-Forwarded-For")).thenReturn(expectedIp);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "test", false));

            // When
            duplicateCheckAspect.checkLimit(joinPoint);

            // Then
            verify(request).getHeader("X-Forwarded-For");
            verify(request, never()).getRemoteAddr();
        }

        @Test
        @DisplayName("모든 헤더가 없으면 RemoteAddr를 반환한다")
        void whenNoHeaders_thenReturnRemoteAddr() throws Throwable {
            // Given
            String expectedIp = "127.0.0.1";
            when(request.getHeader(anyString())).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(expectedIp);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "test", false));

            // When
            duplicateCheckAspect.checkLimit(joinPoint);

            // Then
            verify(request).getRemoteAddr();
        }
    }
}