package com.yhs.blog.springboot.jpa.aop.log;

import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import lombok.extern.log4j.Log4j2;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
@Order(1)
public class LoggingAspect {

    @Around("@annotation(Loggable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        log.info("[LoggingAspect] logExecutionTime() 메서드 시작");


        // 이렇게 처리하면 @ReateLimitAspect에서 발생시킨 예외의 클래스명, 메서드명이 출력되지 않고 대상 메서드로 출력됨
        // String className = joinPoint.getTarget().getClass().getSimpleName();
        // String methodName = joinPoint.getSignature().getName();

        try {
            return joinPoint.proceed();
        } catch (BusinessException ex) {
            log.warn("[{}] Class: {}, Method: {}, Status: {}, Business Error Message: {}, Cause: {}",
                    ex.getErrorCode().getCode(), ex.getClassName(), ex.getMethodName(), ex.getErrorCode().getStatus(),
                    ex.getErrorCode().getMessage(), ex.getCause(), ex);
            throw ex; // 예외를 다시 던져주어 GlobalExceptionHandler에서 처리
        } catch (SystemException ex) {
            log.error("[{}] Class: {}, Method: {}, Status: {}, Business Error Message: {}, Cause: {}",
                    ex.getErrorCode().getCode(), ex.getClassName(), ex.getMethodName(), ex.getErrorCode().getStatus(),
                    ex.getErrorCode().getMessage(), ex.getCause(), ex);
            throw ex;
        }
    }
}
