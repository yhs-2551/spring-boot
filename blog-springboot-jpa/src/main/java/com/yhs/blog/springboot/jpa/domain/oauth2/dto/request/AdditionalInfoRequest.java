package com.yhs.blog.springboot.jpa.domain.oauth2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class AdditionalInfoRequest {
    @NotBlank(message = "사용자명을 입력해주세요")
    @Size(min = 2, max = 10, message = "사용자명은 2-10자 사이여야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "사용자명은 한글(자음, 모음 불가), 영문, 숫자만 사용 가능합니다")
    private String username;

    @NotBlank(message = "블로그의 고유 ID를 입력해주세요")
    @Pattern(regexp = "^[a-z0-9-_]{3,20}$", message = "영문 소문자, 숫자, 하이픈, 언더스코어만 사용 가능합니다 (3-20자)")
    private String blogId;

    private String tempOAuth2UserUniqueId;
}
