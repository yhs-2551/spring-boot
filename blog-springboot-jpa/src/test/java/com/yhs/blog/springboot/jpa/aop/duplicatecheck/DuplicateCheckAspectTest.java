package com.yhs.blog.springboot.jpa.aop.duplicatecheck;

import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
 
@ExtendWith(MockitoExtension.class)
class DuplicateCheckAspectTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    DuplicateCheck duplicateCheck;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes attributes;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private DuplicateCheckAspect duplicateCheckAspect;

    private static final String TEST_IP = "127.0.0.1";

    @BeforeEach
    void setUp() {

        when(duplicateCheck.type()).thenReturn("Test");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Nested
    @DisplayName("중복 체크 시도 횟수 제한 테스트 내부 클래스")
    class AttemptLimitTest {

        @BeforeEach
        void setUp() {

            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
            when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(TEST_IP);

        }

        @Test
        @DisplayName("이메일, 블로그 고유 ID, 사용자명 중 최대 시도 횟수를 초과하면 접근 제한 테스트 및 최대 횟수 초과하지 않은 그 외 최대 횟수 " +
                "적용 안받고 처음부터 진행하는지 체크")
        void 접근_제한_테스트_및_그외_접근_제한_미적용_테스트() throws Throwable {
            // Given 초기 Email 3번 시도

            String emailAttemptKey = "duplicateTestCheck:" + TEST_IP;
            String firstEmailLimitKey = "firstTestLimit:" + TEST_IP;

            when(valueOperations.get(emailAttemptKey)).thenReturn("3");
            when(valueOperations.get(firstEmailLimitKey)).thenReturn(null);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_CHECK_LIMIT_EXCEEDED);
            assertThat(exception.getMessage()).contains("너무 많은 시도입니다");
            verify(redisTemplate).expire(emailAttemptKey, 1L, TimeUnit.MINUTES);
            verify(joinPoint, never()).proceed();

            // Given - 블로그ID로 2번 시도, 이메일 시도와 관련 없이 중복확인 횟수가 카운팅 되는지

            String blogIdAttemptKey = "duplicateBlogIdCheck:" + TEST_IP;
            when(valueOperations.get(blogIdAttemptKey)).thenReturn("2");

            // BlogId 체크용 어노테이션 모킹
            DuplicateCheck blogIdCheck = mock(DuplicateCheck.class);
            when(blogIdCheck.type()).thenReturn("BlogId");

            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "사용 가능한 BlogId 입니다."));

            DuplicateCheckResponse blogIdResponse = duplicateCheckAspect.checkLimit(joinPoint, blogIdCheck);

            // 블로그ID 체크 검증 - 아직 제한에 걸리지 않음
            verify(joinPoint).proceed(); // proceed 호출 검증
            assertThat(blogIdResponse.getMessage()).contains("사용 가능한 BlogId 입니다.");

        }

        @Test
        @DisplayName("최대 시도 횟수 초과 시 첫 번째에만 제한 설정이 된다")
        void 최대시도_횟수_초과시_처음에만_제한설정() throws Throwable {
            // Given
            String attemptKey = "duplicateTestCheck:" + TEST_IP;
            String firstLimitKey = "firstTestLimit:" + TEST_IP;

            // 첫 번째 시도 설정 (제한 걸림, 최초 제한)
            when(valueOperations.get(attemptKey)).thenReturn("3");
            when(valueOperations.get(firstLimitKey)).thenReturn(null);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_CHECK_LIMIT_EXCEEDED);
            assertThat(exception.getMessage()).contains("너무 많은 시도입니다");
            verify(redisTemplate).expire(attemptKey, 1L, TimeUnit.MINUTES);
            verify(valueOperations, times(1)).set(firstLimitKey, "true");
            verify(joinPoint, never()).proceed();

            // Given - 두 번째 시도 설정 (이미 제한 설정됨)
            when(valueOperations.get(firstLimitKey)).thenReturn("true");

            // When - 두 번째 시도
            BusinessException exception2 = assertThrows(BusinessException.class, () -> {
                duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);
            });
            // Then - 두 번째 시도 검증
            assertThat(exception2.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_CHECK_LIMIT_EXCEEDED);
            assertThat(exception2.getMessage()).contains("너무 많은 시도입니다");
            ;

            // firstLimitKey가 두 번째로 설정되지 않았는지 검증
            verify(valueOperations, times(1)).set(firstLimitKey, "true");
            verify(redisTemplate, times(1)).expire(firstLimitKey, 1, TimeUnit.MINUTES);
            verify(redisTemplate, times(1)).expire(attemptKey, 1, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("첫 시도시 사용자가 존재하지 않는 경우, 캐시가 생성된다")
        void 첫시도시_사용자가_존재하지않는경우_캐시_생성() throws Throwable {
            // Given
            String attemptKey = "duplicateTestCheck:" + TEST_IP;

            when(valueOperations.get(attemptKey)).thenReturn(null);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "없음"));

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then
            assertThat(response.isExist()).isFalse();
            assertThat(response.getMessage()).isEqualTo("없음");
            verify(valueOperations).set(attemptKey, "1", 1L, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("첫 시도가 아니고 존재하지 않는 경우, 기존의 캐시값이 증가한다")
        void 첫시도가_아니고_존재하지않는경우_기존_캐시값_증가() throws Throwable {
            // Given
            String attemptKey = "duplicateTestCheck:" + TEST_IP;

            when(valueOperations.get(attemptKey)).thenReturn("2");
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "없음"));

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then
            assertThat(response.isExist()).isFalse();
            assertThat(response.getMessage()).isEqualTo("없음");
            verify(valueOperations).increment(attemptKey);
        }

        @Test
        @DisplayName("첫 시도시 사용자가 존재하는 경우 캐시가 생성된다")
        void 첫시도_및_사용자가_존재하는경우_캐시_생성() throws Throwable {
            // Given
            String attemptKey = "duplicateTestCheck:" + TEST_IP;

            when(valueOperations.get(attemptKey)).thenReturn(null);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(true, "존재함"));

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then
            assertThat(response.isExist()).isTrue();
            verify(valueOperations).set(attemptKey, "1", 1L, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("첫 시도가 아니고, 존재하는 경우 캐시가 증가한다")
        void 첫시도가_아니고_존재하는_경우_캐시값_증가() throws Throwable {
            // Given
            String attemptKey = "duplicateTestCheck:" + TEST_IP;

            when(valueOperations.get(attemptKey)).thenReturn("2");
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(true, "존재함"));

            // When
            DuplicateCheckResponse response = duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then
            assertThat(response.isExist()).isTrue();
            verify(valueOperations).increment(attemptKey);
        }

    }

    @Nested
    @DisplayName("클라이언트 IP 추출 테스트 내부 클래스")
    class ClientIpTest {

        @Test
        @DisplayName("X-Forwarded-For 헤더가 있으면 해당 IP를 반환한다")
        void X_Forwarded_For_헤더_존재시_해당값_반환() throws Throwable {
            // Given
            String expectedIp = "192.168.1.1";
            when(request.getHeader("X-Forwarded-For")).thenReturn(expectedIp);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "test"));

            // When
            duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then
            verify(request).getHeader("X-Forwarded-For");
            verify(request, never()).getRemoteAddr();
        }

        @Test
        @DisplayName("모든 헤더가 없으면 RemoteAddr를 반환한다")
        void 모든_헤더_부재시_RemoteAddr_반환() throws Throwable {
            // Given
            String expectedIp = "127.0.0.1";
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
            when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(expectedIp);
            when(joinPoint.proceed()).thenReturn(new DuplicateCheckResponse(false, "test"));

            // When
            duplicateCheckAspect.checkLimit(joinPoint, duplicateCheck);

            // Then
            verify(request).getRemoteAddr();
        }
    }

}