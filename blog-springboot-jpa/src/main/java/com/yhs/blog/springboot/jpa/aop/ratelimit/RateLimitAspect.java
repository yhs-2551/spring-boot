package com.yhs.blog.springboot.jpa.aop.ratelimit;

import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.concurrent.TimeUnit;

@Log4j2
@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(RateLimit)")

    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        log.info("[RateLimitAspect] checkRateLimit() 메서드 시작");

        String clientIp = getClientIp();
        String rateLimitKey = "rateLimit" + rateLimit.key() + ":" + clientIp; // rateLimitVerifyCode:127.0.0.1
        // 최초 제한인지 확인하기 위한 키. firstRateLimitVerifyCode:127.0.0.1
        String firstRateLimitKey = "firstRateLimit" + rateLimit.key() + ":" + clientIp;

        // 1. 실행 전 체크
        String attempts = redisTemplate.opsForValue().get(rateLimitKey);

        if (attempts != null && Integer.parseInt(attempts) >= rateLimit.maxAttempts()) {

            log.info("[RateLimitAspect] checkRateLimit() 최대 횟수를 초과했을때 분기 진행");

            boolean isFirstRateLimit = redisTemplate.opsForValue().get(firstRateLimitKey) == null;

            if (isFirstRateLimit) {

                log.info("[RateLimitAspect] checkRateLimit() 최대 횟수를 초과 했을 때 분기 진행 - 최초o");

                redisTemplate.opsForValue().set(firstRateLimitKey, "true");
                redisTemplate.expire(firstRateLimitKey, rateLimit.windowMinutes(), TimeUnit.MINUTES);
                redisTemplate.expire(rateLimitKey, rateLimit.windowMinutes(), TimeUnit.MINUTES);
            }

            log.info("[RateLimitAspect] checkRateLimit() 최대 횟수를 초과 했을 때 분기 진행 - 최초x");

            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED, "너무 많은 시도입니다. 1분 후 다시 시도해주세요.",
                    "RateLimitAspect", "checkRateLimit");

        }

        try {

            log.info("[RateLimitAspect] checkRateLimit() 최대 횟수를 초과하지 않았을 때 분기 진행 및 대상 메서드 실행");

            // 2. 대상 메서드 실행
            // login 컨트롤러에 적용한 aop 적용시 AuthenticationManager에 의해 인증이 실패하면 아래 catch문이 실행될 수
            // 있도록 try-catch문으로 감싸줌
            Object result = joinPoint.proceed();

            log.info("[RateLimitAspect] checkRateLimit() 대상 메서드 실행 완료 후 분기 진행");

            // 3. 실행 후 카운트 증가

            updateRateLimitAttempts(rateLimitKey, attempts, rateLimit.windowMinutes());

            return result;

        } catch (AuthenticationException e) { // 로그인의 경우 대상 메서드에서, 인증이 실패하면 AuthenticationException 발생

            log.info("[RateLimitAspect] checkRateLimit() - UserController login 대상 메서드 실행 후 로그인 실패 분기 진행");

            updateRateLimitAttempts(rateLimitKey, attempts, rateLimit.windowMinutes());

            // 횟수만 증가 후, AuthController에서 처리할 수 있도록 예외를 다시 던짐(단일 책임 원칙으로 여기선 횟수 관리만 진행)
            throw e;

        }

    }

    private String getClientIp() {

        log.info("[RateLimitAspect] getClientIp() 메서드 시작");

        // 현재 요청의 RequestAttributes를 가져옴. ThreadLocal을 통해 현재 스레드의 요청 객체 획득
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {

            log.info("[RateLimitAspect] getClientIp() 메서드 - RequestAttributes가 null일 때(웹 요청 컨텍스트 없음) 분기 진행");

            throw new SystemException(
                    ErrorCode.REQUEST_CONTEXT_NOT_FOUND,
                    "웹 요청 컨텍스트를 찾을 수 없습니다.",
                    "RateLimitAspect",
                    "getClientIp");

        }

        // 실제 HttpServletRequest 객체 추출
        HttpServletRequest request = attributes.getRequest();

        // 요청 정보 로깅
        log.info("[RateLimitAspect] getClientIp() 메서드 - RequestAttributes가 null이 아닐때 Remote Address: {}",
                request.getRemoteAddr());

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

    // static으로 하면 인스턴스 멤버(필드, 메서드) 특히 인스턴스 필드인 redisTemplate에 접근할 수 없음.
    private void updateRateLimitAttempts(String rateLimitKey, String attempts, long windowMinutes) {

        log.info("[RateLimitAspect] updateRateLimitAttempts() 메서드 시작 - 시도 횟수 증가");

        if (attempts != null) {
            redisTemplate.opsForValue().increment(rateLimitKey);
        } else {
            redisTemplate.opsForValue().set(rateLimitKey, "1", windowMinutes, TimeUnit.MINUTES);
        }
    }
}