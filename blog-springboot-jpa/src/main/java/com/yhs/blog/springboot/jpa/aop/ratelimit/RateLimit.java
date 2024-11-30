// 인증코드 검증, 인증코드 재발급, 로그인, 비밀번호 재설정 총 4가지 경우에 사용 예정

package com.yhs.blog.springboot.jpa.aop.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String key();  // 메서드별 고유 키 접두사
    int maxAttempts() default 3;
    long windowMinutes() default 1;
}