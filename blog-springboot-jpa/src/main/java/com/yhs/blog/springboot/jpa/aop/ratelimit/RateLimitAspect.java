package com.yhs.blog.springboot.jpa.aop.ratelimit;

import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
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
        String rateLimitKey = String.format("%s:%s", rateLimit.key(), clientIp);

        // 1. 실행 전 체크
        String attempts = redisTemplate.opsForValue().get(rateLimitKey);
        if (attempts != null && Integer.parseInt(attempts) >= rateLimit.maxAttempts()) {
            redisTemplate.expire(rateLimitKey, rateLimit.windowMinutes(), TimeUnit.MINUTES);
            return new RateLimitResponse(false, "너무 많은 시도입니다. 1분 후에 다시 시도해주세요.",
                    HttpStatus.TOO_MANY_REQUESTS.value(), null);
        }
 
        // 2. 대상 메서드 실행
        Object result = joinPoint.proceed();

        // 3. 실행 후 카운트 증가
        if (attempts != null) {
            redisTemplate.opsForValue().increment(rateLimitKey);
        } else {
            redisTemplate.opsForValue().set(rateLimitKey, "1",
                    rateLimit.windowMinutes(), TimeUnit.MINUTES);
        }

        return result;
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