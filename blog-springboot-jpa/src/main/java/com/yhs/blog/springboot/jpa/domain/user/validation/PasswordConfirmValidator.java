package com.yhs.blog.springboot.jpa.domain.user.validation;

import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PasswordConfirmValidator implements ConstraintValidator<ValidPasswordConfirm, SignUpUserRequest> {

    @Override
    public void initialize(ValidPasswordConfirm constraintAnnotation) {
    }

    @Override
    public boolean isValid(SignUpUserRequest request, ConstraintValidatorContext context) {

        log.info("request.getPassword() : {}", request.getPassword());
        log.info("request.getPasswordConfirm() : {}", request.getPasswordConfirm());


        if (request.getPasswordConfirm() == null) {
            return true; // 이 부분은 passwordConfirm 필드의 @NotBlank로 처리
        }

        if (!(request.getPassword().equals(request.getPasswordConfirm()))) {
            context.disableDefaultConstraintViolation(); // 기본 제약조건을 비활성화
            context.buildConstraintViolationWithTemplate("비밀번호가 일치하지 않습니다.")
                    .addPropertyNode("passwordConfirm")  // 특정 필드에 에러 바인딩
                    .addConstraintViolation();
            return false;
        }

        return true;

    }
}