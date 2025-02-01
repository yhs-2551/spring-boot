package com.yhs.blog.springboot.jpa.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.VerifyEmailRequest;
import com.yhs.blog.springboot.jpa.common.response.RateLimitResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    EmailSender emailSender;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserRegistrationService userRegistrationService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("회원가입 버튼 클릭 시 이메일로 인증코드 발급 내부 클래스")
    class ProcessSignUp {
        @Test
        @DisplayName("회원가입 버튼 클릭 시 이메일 인증코드 발급 성공")
        void 회원가입_버튼_클릭시_이메일로_인증코드_발급_성공() throws JsonProcessingException {
            // given
            SignUpUserRequest request = new SignUpUserRequest(
                    "test__",
                    "테스트123",
                    "test@example.com",
                    "TestPassword123*",
                    "TestPassword123*");

            when(emailSender.sendEmail(any(), any(), any())).thenReturn(true);

            // when
            RateLimitResponse<Void> response = emailService.processEmailVerification(request);

            // then
            assertThat(response.isSuccess()).isTrue();
            verify(emailSender).sendEmail(any(), any(), any());
            verify(valueOperations, times(2)).set(any(), any(), any(Duration.class));
        }

    }

    @Nested
    @DisplayName("인증코드 검증 클래스")
    class CompleteVerification {

        private static final String TEST_BLOG_ID = "test_blog_id";
        private static final String TEST_CODE = "123456";
        private static final String VERIFICATION_CODE_PREFIX = "verificationCode:";
        private static final String TEMP_USER_PREFIX = "tempUser:";

        @Test
        @DisplayName("인증코드 검증 시 만료된 인증코드로 인한 실패 응답")
        void 인증코드_검증시_만료된_인증코드() {
            // given
            VerifyEmailRequest request = new VerifyEmailRequest(TEST_BLOG_ID, TEST_CODE);
            when(valueOperations.get(VERIFICATION_CODE_PREFIX + TEST_BLOG_ID)).thenReturn(null);

            // when
            RateLimitResponse<Void> response = emailService.completeVerification(request);

            // then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("만료된 인증코드입니다. 인증코드를 재발급 받아주세요.");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE.value());
        }

        @Test
        @DisplayName("인증코드 검증 시 유효하지 않은 인증코드로 인한 실패 응답")
        void 인증코드_검증시_유효하지_않은_인증코드() {
            // given
            VerifyEmailRequest request = new VerifyEmailRequest(TEST_BLOG_ID, "wrong");
            when(valueOperations.get(VERIFICATION_CODE_PREFIX + TEST_BLOG_ID)).thenReturn(TEST_CODE);

            // when
            RateLimitResponse<Void> response = emailService.completeVerification(request);

            // then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("인증코드가 유효하지 않습니다.");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("인증코드 검증 시 인증 성공 응답")
        void 인증코드_검증시_유효한_인증코드_사용자_회원가입_성공() throws JsonProcessingException {
            // given
            VerifyEmailRequest request = new VerifyEmailRequest("test_blog_id", "123456");

            when(valueOperations.get(VERIFICATION_CODE_PREFIX + request.getBlogId())).thenReturn(TEST_CODE);
            when(valueOperations.get(TEMP_USER_PREFIX + request.getBlogId())).thenReturn("{}");
            when(objectMapper.readValue(anyString(), eq(SignUpUserRequest.class))).thenReturn(new SignUpUserRequest());
            doNothing().when(userRegistrationService).createUser(any(SignUpUserRequest.class));

            // when
            RateLimitResponse<Void> response = emailService.completeVerification(request);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("회원가입에 성공하였습니다.");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
            verify(userRegistrationService).createUser(any(SignUpUserRequest.class));
            verify(redisTemplate, times(2)).delete(anyString());
        }

    }

}