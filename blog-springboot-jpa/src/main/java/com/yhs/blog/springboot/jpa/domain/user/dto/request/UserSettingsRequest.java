package com.yhs.blog.springboot.jpa.domain.user.dto.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSettingsRequest(
                @NotBlank(message = "사용자명을 입력해주세요") @Size(min = 2, max = 10, message = "사용자명은 2-10자 사이여야 합니다") @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "사용자명은 한글(자음, 모음 불가), 영문, 숫자만 사용 가능합니다") String username,
                @NotBlank(message = "블로그 이름을 입력해주세요") @Size(max = 32, message = "블로그 이름은 최대 32자까지 가능합니다") String blogName,
                MultipartFile profileImage) {

}
