// package com.yhs.blog.springboot.jpa.domain.user.service;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
// import com.yhs.blog.springboot.jpa.domain.user.dto.request.VerifyEmailRequest;
// import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
// import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.core.ValueOperations;
// import org.springframework.http.HttpStatus;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;

// import java.time.Duration;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class EmailServiceTest {

//     @InjectMocks
//     private EmailService emailService;

//     @Mock
//     private JavaMailSender mailSender;

//     @Mock
//     private RedisTemplate<String, String> redisTemplate;

//     @Mock
//     private ObjectMapper objectMapper;

//     @Mock
//     private UserService userService;

//     @Mock
//     private ValueOperations<String, String> valueOperations;

//     @BeforeEach
//     void setUp() {
//         when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//     }

//     @Nested
//     @DisplayName("processSignUp 메서드의 회원가입 버튼 클릭 시 이메일 인증코드 발급 테스트 클래스")
//     class ProcessSignUp {
//         @Test
//         @DisplayName("회원가입 버튼 클릭 시 이메일 인증코드 발급 성공 테스트")
//         void 인증코드_발급_성공() throws JsonProcessingException {
//             // given
//             SignUpUserRequest request = new SignUpUserRequest();
//             request.setEmail("test@test.com");
//             request.setUsername("testuser");

//             // when
//             RateLimitResponse response = emailService.processSignUp(request);

//             // then
//             assertThat(response.isSuccess()).isTrue();
//             verify(mailSender).send(any(SimpleMailMessage.class));
//             verify(valueOperations, times(2)).set(any(), any(), any(Duration.class));
//             verify(redisTemplate, times(2)).delete(anyString());
//         }

//     }


//     @Nested
//     @DisplayName("completeVerification 메서드의 인증코드 컴증 테스트 클래스")
//     class CompleteVerification {

//         private static final String TEST_EMAIL = "test@test.com";
//         private static final String TEST_CODE = "123456";
//         private static final String VERIFICATION_CODE_PREFIX = "verificationCode:";
//         private static final String TEMP_USER_PREFIX = "tempUser:";

//         @Test
//         @DisplayName("만료된 인증코드")
//         void 만료된_인증코드_테스트() {
//             // given
//             VerifyEmailRequest request = new VerifyEmailRequest(TEST_EMAIL, TEST_CODE);
//             when(valueOperations.get(VERIFICATION_CODE_PREFIX + TEST_EMAIL)).thenReturn(null);

//             // when
//             RateLimitResponse response = emailService.completeVerification(request);

//             // then
//             assertThat(response.isSuccess()).isFalse();
//             assertThat(response.getMessage()).isEqualTo("만료된 인증코드입니다. 인증코드를 재발급 받아주세요.");
//             assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE.value());
//         }

//         @Test
//         @DisplayName("유효하지 않은 인증코드")
//         void 유효하지_않은_인증코드_테스트() {
//             // given
//             VerifyEmailRequest request = new VerifyEmailRequest(TEST_EMAIL, "wrong");
//             when(valueOperations.get(VERIFICATION_CODE_PREFIX + TEST_EMAIL)).thenReturn(TEST_CODE);

//             // when
//             RateLimitResponse response = emailService.completeVerification(request);

//             // then
//             assertThat(response.isSuccess()).isFalse();
//             assertThat(response.getMessage()).isEqualTo("인증코드가 유효하지 않습니다.");
//             assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
//         }

//         @Test
//         @DisplayName("인증 성공 테스트")
//         void 사용자_계정_생성_성공() throws JsonProcessingException {
//             // given
//             VerifyEmailRequest request = new VerifyEmailRequest("test@test.com", "123456");

//             SignUpUserRequest signUpRequest = new SignUpUserRequest();
//             SignUpUserResponse signUpResponse = new SignUpUserResponse();
//             when(valueOperations.get(VERIFICATION_CODE_PREFIX + request.getEmail())).thenReturn(TEST_CODE);
//             when(valueOperations.get(TEMP_USER_PREFIX + request.getEmail())).thenReturn("{}");
//             when(objectMapper.readValue(anyString(), eq(SignUpUserRequest.class))).thenReturn(signUpRequest);
//             when(userService.createUser(any(SignUpUserRequest.class))).thenReturn(signUpResponse);

//             // when
//             RateLimitResponse response = emailService.completeVerification(request);

//             // then
//             assertThat(response.isSuccess()).isTrue();
//             assertThat(response.getMessage()).isEqualTo("사용자 계정 생성에 성공하였습니다.");
//             assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
//             verify(userService).createUser(any(SignUpUserRequest.class));
//             verify(redisTemplate, times(2)).delete(anyString());
//         }

//     }

// }