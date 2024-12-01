package com.yhs.blog.springboot.jpa.domain.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "비밀번호가 유효하지 않습니다";
    Class<?>[] groups() default {}; // {}는 기본값으로 빈 배열을 의미함.
    Class<? extends Payload>[] payload() default {};
}
