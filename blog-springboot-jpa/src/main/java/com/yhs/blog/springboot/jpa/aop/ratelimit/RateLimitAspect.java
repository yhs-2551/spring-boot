package com.yhs.blog.springboot.jpa.aop.ratelimit;

import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RedisTemplate<String, String> redisTemplate;
    private final HttpServletRequest request;  // IP 기반 키 생성용

    @Around("@annotation(RateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String clientIp = getClientIp();
        String rateLimitKey = "rateLimit" + rateLimit.key() + ":" + clientIp; // rateLimitVerifyCode:127.0.0.1
        // 최초 제한인지 확인하기 위한 키. firstRateLimitVerifyCode:127.0.0.1
        String firstRateLimitKey = "firstRateLimit" + rateLimit.key() + ":" + clientIp;

        // 1. 실행 전 체크
        String attempts = redisTemplate.opsForValue().get(rateLimitKey);

        if (attempts != null && Integer.parseInt(attempts) >= rateLimit.maxAttempts()) {

            boolean isFirstRateLimit = redisTemplate.opsForValue().get(firstRateLimitKey) == null;

            if (isFirstRateLimit) {
                redisTemplate.opsForValue().set(firstRateLimitKey, "true");
                redisTemplate.expire(firstRateLimitKey, rateLimit.windowMinutes(), TimeUnit.MINUTES);
                redisTemplate.expire(rateLimitKey, rateLimit.windowMinutes(), TimeUnit.MINUTES);
            }

            return new RateLimitResponse(false, "너무 많은 시도입니다. 1분 후에 다시 시도해주세요.",
                    HttpStatus.TOO_MANY_REQUESTS.value(), null);
        }
 
        // 2. 대상 메서드 실행
        Object result = joinPoint.proceed();
        RateLimitResponse rateLimitResponse = (RateLimitResponse) result;

        // 성공하면 아래 attempts 관련 코드 실행할 필요 없이 바로 해당 응답 return.
        if (rateLimitResponse.isSuccess()) {
            return rateLimitResponse;
        }

        // 3. 실행 후 카운트 증가
        if (attempts != null) {
            redisTemplate.opsForValue().increment(rateLimitKey);
        } else {
            redisTemplate.opsForValue().set(rateLimitKey, "1",
                    rateLimit.windowMinutes(), TimeUnit.MINUTES);
        }

        return rateLimitResponse;
    }

    private String getClientIp() {
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
}