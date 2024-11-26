package com.yhs.blog.springboot.jpa.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.aop.ratelimit.RateLimit;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.VerifyEmailRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    private static final String VERIFICATION_CODE_PREFIX = "verificationCode:";
    private static final String TEMP_USER_PREFIX = "tempUser:";
    private static final String VERIFICATION_ATTEMPT_PREFIX = "verificationAttempt:";
    private static final int MAX_ATTEMPTS = 3;
    private static final long WINDOW_MINUTES = 1;

    public RateLimitResponse processSignUp(SignUpUserRequest signUpUserRequest) {

        try {

            // 기존 인증 코드가 남아있다면 삭제. 인증코드 재발급까지 한번에 처리하기 위함
            redisTemplate.delete(VERIFICATION_CODE_PREFIX + signUpUserRequest.getEmail());
            redisTemplate.delete(TEMP_USER_PREFIX + signUpUserRequest.getEmail());

            String verificationCode = generateVerificationCode();

            String subject = "Welcome to Blog! Your Verification Code Inside";
            String text = String.format("Hello %s, Welcome to our Blog service. Please use the following verification code to complete your registration: %s",
                    signUpUserRequest.getUsername(),
                    verificationCode);


            try {
                sendEmail(signUpUserRequest.getEmail(), subject, text);
            } catch (MailException e) {
                return new RateLimitResponse(false, "이메일 발송에 실패했습니다.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
            }

            redisTemplate.opsForValue().set(VERIFICATION_CODE_PREFIX + signUpUserRequest.getEmail(), verificationCode,
                    Duration.ofMinutes(3)); // 3분안에 인증 코드를 입력해야함.

            redisTemplate.opsForValue().set(TEMP_USER_PREFIX + signUpUserRequest.getEmail(),
                    objectMapper.writeValueAsString(signUpUserRequest), Duration.ofMinutes(3));

            return new RateLimitResponse(true, "이메일 인증 코드가 발송되었습니다.", HttpStatus.OK.value(), null);

        } catch (JsonProcessingException e) {
            return new RateLimitResponse(false, "인증 처리 중 오류가 발생하였습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        }

    }


    //    userService.createUser와 redis의 동시 작업을 안전하게 하기 위해 @Transcational 어노테이션 추가
    @Transactional
    public RateLimitResponse completeVerification(VerifyEmailRequest verifyEmailRequest) {

        // json 관련 objectMapper 메서드 사용 시 try-catch 문으로 예외 처리 필요
        try {
            return verifyEmailAuthenticationCode(verifyEmailRequest.getEmail(),
                    verifyEmailRequest.getCode());

        } catch (JsonProcessingException e) {
            return new RateLimitResponse(false, "인증 처리 중 오류가 발생하였습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
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


    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);// 이메일의 제목
        message.setText(text);
        mailSender.send(message);
    }


    // 총 3번의 시도 후 4번째 시도부터 1분 뒤 재요청 해야함
    @RateLimit(key = "verifyCode")
    private RateLimitResponse verifyEmailAuthenticationCode(String email, String code) throws JsonProcessingException {

        String saveCode = redisTemplate.opsForValue().get(VERIFICATION_CODE_PREFIX + email);
        if (saveCode == null) {
            return new RateLimitResponse(false, "만료된 인증코드입니다. 1분 후에 다시 요청해주세요.",
                    HttpStatus.GONE.value(), null);
        }

        if (!saveCode.equals(code)) {
            return new RateLimitResponse(false, "인증코드가 유효하지 않습니다.",
                    HttpStatus.BAD_REQUEST.value(), null);
        } else {

            String userJson = redisTemplate.opsForValue().get(TEMP_USER_PREFIX + email);

            SignUpUserRequest signUpUserRequest = objectMapper.readValue(userJson, SignUpUserRequest.class);

            SignUpUserResponse response = userService.createUser(signUpUserRequest);

            redisTemplate.delete(TEMP_USER_PREFIX + email);
            redisTemplate.delete(VERIFICATION_CODE_PREFIX + email);

            return new RateLimitResponse(true, "사용자 계정 생성에 성공하였습니다.", HttpStatus.CREATED.value(), response);
        }

    }


}
