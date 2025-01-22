package com.yhs.blog.springboot.jpa.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.aop.performance.MeasurePerformance;
import com.yhs.blog.springboot.jpa.aop.ratelimit.RateLimit;
import com.yhs.blog.springboot.jpa.common.constant.code.StatusCode;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.VerifyEmailRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse; 

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final EmailSender emailSender;

    private static final String VERIFICATION_CODE_PREFIX = "verificationCode:";
    private static final String TEMP_USER_PREFIX = "tempUser:";

    @Loggable
    @RateLimit(key = "IssueCode") // 총 3번의 시도 후 4번째 시도부터 1분 뒤 재요청 해야함
    public RateLimitResponse<Void> processEmailVerification(SignUpUserRequest signUpUserRequest) {

        // 기존 인증 코드가 남아있다면 삭제. 인증코드 재발급까지 한번에 처리하기 위함
        // redisTemplate.delete(VERIFICATION_CODE_PREFIX +
        // signUpUserRequest.getEmail());
        // redisTemplate.delete(TEMP_USER_PREFIX + signUpUserRequest.getEmail());

        String verificationCode = generateVerificationCode();

        String subject = "블로그에 오신 것을 환영합니다! 인증 코드가 포함되어 있습니다.";

        String text = String.format(
                """
                        %s님 안녕하세요!
                        블로그 회원가입 인증 코드: %s
                        3분 내에 인증을 완료해주세요.
                        """,
                signUpUserRequest.getUsername(),
                verificationCode);

        boolean emailResult = emailSender.sendEmail(signUpUserRequest.getEmail(), subject, text);

        try {
            if (emailResult) {

                redisTemplate.opsForValue().set(VERIFICATION_CODE_PREFIX + signUpUserRequest.getBlogId(),
                        verificationCode,
                        Duration.ofMinutes(3)); // 3분안에 인증 코드를 입력해야함.

                redisTemplate.opsForValue().set(TEMP_USER_PREFIX + signUpUserRequest.getBlogId(),
                        objectMapper.writeValueAsString(signUpUserRequest), Duration.ofMinutes(3));

                     

                return new RateLimitResponse<>(true, "이메일 인증 코드가 발송되었습니다.",
                        StatusCode.OK.getCode(), null);

            } else {
                return new RateLimitResponse<>(false, "이메일 발송에 실패했습니다.",
                        StatusCode.INTERNAL_ERROR.getCode(), null);
            }

        } catch (JsonProcessingException e) {
            // objectMapper.writeValueAsString() 메서드 호출 시 발생하는 예외 처리. 객체를 json 문자열로 변환하는
            // 과정에서 예외가 발생할 수 있음
            return new RateLimitResponse<>(false, "인증 처리 중 오류가 발생하였습니다.", StatusCode.INTERNAL_ERROR.getCode(), null);
        }

    }

    @MeasurePerformance
    @Loggable
    @RateLimit(key = "VerifyCode") // 총 3번의 시도 후 4번째 시도부터 1분 뒤 재요청 해야함
    @Transactional // userService.createUser와 redis의 동시 작업을 안전하게 하기 위해 @Transactional 어노테이션 추가, DB
                   // 작업이 있어서 @Transactional 추가
    public RateLimitResponse<Void> completeVerification(VerifyEmailRequest verifyEmailRequest) {

        // json 관련 objectMapper 메서드 사용 시 try-catch 문으로 예외 처리 필요
        try {

            String saveCode = redisTemplate.opsForValue()
                    .get(VERIFICATION_CODE_PREFIX + verifyEmailRequest.getBlogId());
            if (saveCode == null) {
                return new RateLimitResponse<>(false, "만료된 인증코드입니다. 인증코드를 재발급 받아주세요.", StatusCode.GONE.getCode(), null);
            }

            if (!saveCode.equals(verifyEmailRequest.getCode())) {
                return new RateLimitResponse<>(false, "인증코드가 유효하지 않습니다.", StatusCode.BAD_REQUEST.getCode(), null);
            } else {

                String userJson = redisTemplate.opsForValue().get(TEMP_USER_PREFIX + verifyEmailRequest.getBlogId());

                SignUpUserRequest signUpUserRequest = objectMapper.readValue(userJson, SignUpUserRequest.class);

                userService.createUser(signUpUserRequest);

                redisTemplate.delete(TEMP_USER_PREFIX + verifyEmailRequest.getBlogId());
                redisTemplate.delete(VERIFICATION_CODE_PREFIX + verifyEmailRequest.getBlogId());

                return new RateLimitResponse<>(true, "회원가입에 성공하였습니다.", StatusCode.CREATED.getCode(), null);
            }

        } catch (JsonProcessingException e) {
            return new RateLimitResponse<>(false, "인증 처리 중 오류가 발생하였습니다.", StatusCode.INTERNAL_ERROR.getCode(), null);
        }

    }

    private String generateVerificationCode() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(secureRandom.nextInt(10)); // 0 ~ 9의 정수값을 랜덤하게 생성
        }
        return code.toString();
    }

}
