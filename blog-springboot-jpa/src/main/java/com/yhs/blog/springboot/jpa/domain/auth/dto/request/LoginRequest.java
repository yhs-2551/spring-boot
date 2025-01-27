package com.yhs.blog.springboot.jpa.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginRequest {

    @NotBlank(message = "이메일을 입력해주세요")
    @Pattern(
            regexp = "\\S+@\\S+\\.\\S+", // 자바에서 공백은 \s인데 공백을 올바르게 표현하기 위해서 \\S로 표현 -> 이스케이프 사용해야함.
            message = "올바른 이메일 형식이 아닙니다"
    )
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

    @NotNull(message = "로그인 유지 여부를 선택해주세요")
    private Boolean rememberMe;
}
