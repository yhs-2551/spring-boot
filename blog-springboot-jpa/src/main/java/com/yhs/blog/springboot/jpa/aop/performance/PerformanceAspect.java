package com.yhs.blog.springboot.jpa.aop.performance;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import lombok.extern.log4j.Log4j2;

@Aspect
@Component
@Log4j2
public class PerformanceAspect {

    @Around("@annotation(MeasurePerformance)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = joinPoint.proceed();

        stopWatch.stop();
        log.info("실행 시간: {} ms", stopWatch.getTotalTimeMillis());
        
        return result;
    }

}
