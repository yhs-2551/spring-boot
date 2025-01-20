// 회원가입시 중복확인 3회 관련 로직

package com.yhs.blog.springboot.jpa.aop.duplicatecheck;

import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
@Log4j2
public class DuplicateCheckAspect {

    private final RedisTemplate<String, String> redisTemplate;
    private final HttpServletRequest request;

    // private static final String CHECK_ATTEMPT_PREFIX = "duplicateCheck:";
    private static final int MAX_ATTEMPTS = 3;
    private static final long WINDOW_MINUTES = 1L;

    // 실제 대상 메서드를 실행해야 중복확인 시도 증가.
    // Around는 joinPoint.proceed()를 명시적으로 호출해야만 실제 메서드 실행.
    @Around("@annotation(DuplicateCheck)")
    public DuplicateCheckResponse checkLimit(ProceedingJoinPoint joinPoint, DuplicateCheck duplicateCheck)
            throws Throwable {
        String clientIp = getClientIp();
        String checkType = duplicateCheck.type();
        String attemptKey = "duplicate" + checkType + "Check:" + clientIp; // duplicateEmailCheck:127.0.0.1
        String firstLimitKey = "first" + checkType + "Limit:" + clientIp; // 최초 제한인지 확인하기 위한 키.
        // 최초 제한일때만 만료시간을 1분으로 설정

        int attempts;

        // 현재 시도 횟수 확인. 초기 호출일 경우 실제 대상 메서드가 실행 되기 전이니 중복확인 횟수 0
        String currentAttempts = redisTemplate.opsForValue().get(attemptKey);
        attempts = currentAttempts != null ? Integer.parseInt(currentAttempts) : 0;

        // 최대 시도 횟수 체크
        // 최대 시도를 초과하면 만료 시간 새롭게 갱신함으로써 초과한 시점 기준으로 1분 기다려야 재시도 가능
        if (attempts >= MAX_ATTEMPTS) {

            boolean isFirstLimit = redisTemplate.opsForValue().get(firstLimitKey) == null;

            if (isFirstLimit) {
                redisTemplate.opsForValue().set(firstLimitKey, "true");
                redisTemplate.expire(firstLimitKey, WINDOW_MINUTES, TimeUnit.MINUTES);
                redisTemplate.expire(attemptKey, WINDOW_MINUTES, TimeUnit.MINUTES);
            }

            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED, "너무 많은 시도입니다. 1분 후 다시 시도해주세요.",
                    "DuplicateCheckAspect", "checkLimit");

            // return new DuplicateCheckResponse(false, "너무 많은 시도입니다. 1분 후 다시 시도해주세요.", true);
        }

        // 실제 메서드 실행.
        // 실제 메서드 실행 이후에 중복확인 횟수를 증가시킨다. 실제 메서드 이전에 중복확인 횟수를 증가시키면, 중복 확인 숫자만 증가하고 실제
        // 메서드는 실행되지
        // 않는 경우가 있을 수 있기 때문.
        Object result = joinPoint.proceed();
        DuplicateCheckResponse response = (DuplicateCheckResponse) result;

        // 이미 존재하든 존재하지 않든 중복 체크 관련 로직은 동일하게 1분에 최대 3회로 적용
        if (!response.isExist()) {
            return handleDuplicateCheck(response, attemptKey, attempts);
        } else {
            return handleDuplicateCheck(response, attemptKey, attempts);
        }

    }

    private String getClientIp() {
        // HttpServletRequest request =
        // ((ServletRequestAttributes)
        // RequestContextHolder.currentRequestAttributes()).getRequest();

        String clientIp = request.getHeader("X-Forwarded-For");

        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }

        return clientIp;
    }

    private DuplicateCheckResponse handleDuplicateCheck(DuplicateCheckResponse response, String attemptKey,
            int attempts) {
        // 시도 횟수 증가 처리
        if (attempts == 0) {
            redisTemplate.opsForValue().set(attemptKey, "1", WINDOW_MINUTES, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().increment(attemptKey);
        }

        return new DuplicateCheckResponse(
                response.isExist(),
                response.getMessage());
    }

}
