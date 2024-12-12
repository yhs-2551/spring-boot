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

//    @Mock
//    DuplicateCheck duplicateCheck;

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
        @DisplayName("이메일, 블로그 고유 ID, 사용자명 중 최대 시도 횟수를 초과하면 접근 제한 테스트 및 최대 횟수 초과하지 않은 그 외 최대 횟수 " +
                "적용 안받고 처음부터 진행하는지 체크")
        void whenExceedMaxAttempts_thenBlockAccess() throws Throwable {
            // Given 초기 Email 3번 시도
            String clientIp = "127.0.0.1";
            String emailAttemptKey = "duplicateEmailCheck:" + clientIp;
            String firstEmailLimitKey = "firstEmailLimit:" + clientIp;

            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(emailAttemptKey)).thenReturn("3");
            when(valueOperations.get(firstEmailLimitKey)).thenReturn(null);

            // Email 체크용 어노테이션 모킹
            DuplicateCheck emailCheck = mock(DuplicateCheck.class);
            when(emailCheck.type()).thenReturn("Email");

            DuplicateCheckResponse emailResponse = duplicateCheckAspect.checkLimit(joinPoint, emailCheck);

            // Then
            assertThat(emailResponse.isExist()).isFalse();
            assertThat(emailResponse.getMessage()).contains("너무 많은 시도입니다");
            assertThat(emailResponse.isLimited()).isTrue();
            verify(redisTemplate).expire(emailAttemptKey, 1L, TimeUnit.MINUTES);
            verify(joinPoint, never()).proceed();


            // Given - 블로그ID로 2번 시도, 이메일 시도와 관련 없이 중복확인 횟수가 카운팅 되는지

            String blogIdAttemptKey = "duplicateBlogIdCheck:" + clientIp;
            when(valueOperations.get(blogIdAttemptKey)).thenReturn("2");

            // BlogId 체크용 어노테이션 모킹
            DuplicateCheck blogIdCheck = mock(DuplicateCheck.class);
            when(blogIdCheck.type()).thenReturn("BlogId");

            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "사용 가능합니다", false));

            DuplicateCheckResponse blogIdResponse = duplicateCheckAspect.checkLimit(joinPoint, blogIdCheck);

            // 블로그ID 체크 검증 - 아직 제한에 걸리지 않음
            verify(joinPoint).proceed();  // proceed 호출 검증
            assertThat(blogIdResponse.isLimited()).isFalse();
            assertThat(blogIdResponse.getMessage()).contains("사용 가능합니다");


        }

        @Test
        @DisplayName("최대 시도 횟수 초과 시 첫 번째에만 제한 설정이 된다")
        void whenExceedMaxAttempts_thenSetLimitOnlyOnce() throws Throwable {
            // Given
            String clientIp = "127.0.0.1";
            String attemptKey = "duplicateTestCheck:" + clientIp;
            String firstLimitKey = "firstTestLimit:" + clientIp;

            // Email 체크용 어노테이션 모킹
            DuplicateCheck duplicateCheck = mock(DuplicateCheck.class);
            when(duplicateCheck.type()).thenReturn("Test");
            when(request.getRemoteAddr()).thenReturn(clientIp);

            // 첫 번째 시도 설정 (제한 걸림, 최초 제한)
            when(valueOperations.get(attemptKey)).thenReturn("3");
            when(valueOperations.get(firstLimitKey)).thenReturn(null);

            // When - 첫 번째 시도
            DuplicateCheckResponse firstResponse = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then - 첫 번째 시도 검증
            assertThat(firstResponse.isLimited()).isTrue();
            assertThat(firstResponse.getMessage()).contains("너무 많은 시도입니다");
            verify(valueOperations).set(firstLimitKey, "true");
            verify(redisTemplate).expire(firstLimitKey, 1, TimeUnit.MINUTES);
            verify(redisTemplate).expire(attemptKey, 1, TimeUnit.MINUTES);

            // Given - 두 번째 시도 설정 (이미 제한 설정됨)
            when(valueOperations.get(firstLimitKey)).thenReturn("true");

            // When - 두 번째 시도
            DuplicateCheckResponse secondResponse = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then - 두 번째 시도 검증
            assertThat(secondResponse.isLimited()).isTrue();
            assertThat(secondResponse.getMessage()).contains("너무 많은 시도입니다");
            // firstLimitKey가 두 번째로 설정되지 않았는지 검증
            verify(valueOperations, times(1)).set(firstLimitKey, "true");
            verify(redisTemplate, times(1)).expire(firstLimitKey, 1, TimeUnit.MINUTES);
            verify(redisTemplate, times(1)).expire(attemptKey, 1, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("첫 시도시 사용자가 존재하지 않는 경우, 캐시가 생성된다")
        void whenFirstAttemptAndNotExists_thenCreateCache() throws Throwable {
            // Given
            String clientIp = "127.0.0.1";
            String attemptKey = "duplicateTestCheck:" + clientIp;

            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(attemptKey)).thenReturn(null);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "없음", false));

            DuplicateCheck duplicateCheck = mock(DuplicateCheck.class);
            when(duplicateCheck.type()).thenReturn("Test");
            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

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
            String attemptKey = "duplicateTestCheck:" + clientIp;
            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(attemptKey)).thenReturn("2");
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "없음", false));

            DuplicateCheck duplicateCheck = mock(DuplicateCheck.class);
            when(duplicateCheck.type()).thenReturn("Test");

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then
            assertThat(response.isExist()).isFalse();
            assertThat(response.getMessage()).isEqualTo("없음");
            assertThat(response.isLimited()).isFalse();
            verify(valueOperations).increment(attemptKey);
        }

        @Test
        @DisplayName("첫 시도시 사용자가 존재하는 경우 캐시가 생성된다")
        void whenFirstAttemptAndExists_thenCreateCache() throws Throwable {
            // Given
            String clientIp = "127.0.0.1";
            String attemptKey = "duplicateTestCheck:" + clientIp;

            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(attemptKey)).thenReturn(null);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(true, "존재함", false));

            DuplicateCheck duplicateCheck = mock(DuplicateCheck.class);
            when(duplicateCheck.type()).thenReturn("Test");

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then
            assertThat(response.isExist()).isTrue();
            verify(valueOperations).set(attemptKey, "1", 1L, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("첫 시도가 아니고, 존재하는 경우 캐시가 증가한다")
        void whenNotFirstAttemptAndExists_thenIncreaseCache() throws Throwable {
            // Given
            String clientIp = "127.0.0.1";
            String attemptKey = "duplicateTestCheck:" + clientIp;

            when(request.getRemoteAddr()).thenReturn(clientIp);
            when(valueOperations.get(attemptKey)).thenReturn("2");
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(true, "존재함", false));

            DuplicateCheck duplicateCheck = mock(DuplicateCheck.class);
            when(duplicateCheck.type()).thenReturn("Test");

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

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

            DuplicateCheck duplicateCheck = mock(DuplicateCheck.class);
            when(duplicateCheck.type()).thenReturn("Test");

            // When
            duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

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

            DuplicateCheck duplicateCheck = mock(DuplicateCheck.class);
            when(duplicateCheck.type()).thenReturn("Test");

            // When
            duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then
            verify(request).getRemoteAddr();
        }
    }
}