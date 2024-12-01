package com.yhs.blog.springboot.jpa.domain.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordConfirmValidator.class)
@Target({ElementType.TYPE}) // 클래스 레벨에 적용
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPasswordConfirm {
    String message() default "비밀번호가 일치하지 않습니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}