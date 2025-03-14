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

    @NotBlank(message = "블로그의 고유 ID를 입력해주세요")
    @Pattern(regexp = "^[a-z0-9-_]{3,20}$", message = "영문 소문자, 숫자, 하이픈, 언더스코어만 사용 가능합니다 (3-20자)")
    private String blogId;

    @NotBlank(message = "인증코드를 입력해주세요.")
    @Size(min = 6, max = 6, message = "인증코드는 6자리여야 합니다.")
    private String code;
    
}
