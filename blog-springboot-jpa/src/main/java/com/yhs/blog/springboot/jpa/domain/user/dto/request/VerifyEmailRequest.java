package com.yhs.blog.springboot.jpa.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 아래 유효성 검사도 백엔드에서 우회 요청을 막기 위함. 따로 프론트 응답코드로는 사용x
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailRequest {
    @Pattern(
            regexp = "\\S+@\\S+\\.\\S+", // 자바에서 공백은 \s인데 공백을 올바르게 표현하기 위해서 \\S로 표현 -> 이스케이프 사용해야함.
            message = "올바른 이메일 형식이 아닙니다."
    )
    private String email;

    @NotBlank(message = "인증코드를 입력해주세요.")
    @Size(min = 6, max = 6, message = "인증코드는 6자리여야 합니다.")
    private String code;
}
