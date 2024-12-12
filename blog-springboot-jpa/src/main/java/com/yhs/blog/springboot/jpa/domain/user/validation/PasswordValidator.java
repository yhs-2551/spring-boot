package com.yhs.blog.springboot.jpa.domain.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// 유효성 하는 필드의 타입이 String이기 때문에 String 타입을 받는다고 명시해줌
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // 초기화 로직이 필요하지 않으니 비워둠
    @Override
    public void initialize(ValidPassword constraintAnnotation) {

    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            context.disableDefaultConstraintViolation(); // 기본 제약조건을 비활성화
            context.buildConstraintViolationWithTemplate("비밀번호를 입력해주세요").addConstraintViolation();
            return false;
        }

        if (password.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호에 한글은 사용할 수 없습니다").addConstraintViolation();
            return false;
        }

        if (!password.matches("^[A-Z].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("첫 글자는 반드시 대문자여야 합니다").addConstraintViolation();
            return false;
        }

        if (password.length() < 8) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호는 8자 이상이어야 합니다").addConstraintViolation();
            return false;
        }

        // 비밀번호에 영문 소문자. 숫자. 특수문자를 포함해야함
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        if (!hasLowercase || !hasDigit || !hasSpecialChar) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호에 소문자, 숫자, 특수문자를 포함해야 합니다").addConstraintViolation();
            return false;
        }

        return true;
    }
}
