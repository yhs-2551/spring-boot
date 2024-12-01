package com.yhs.blog.springboot.jpa.domain.user.dto.request;
import com.yhs.blog.springboot.jpa.domain.user.validation.ValidPassword;
import com.yhs.blog.springboot.jpa.domain.user.validation.ValidPasswordConfirm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

// 프론트에서 막아두긴 했지만 아래 DTO에서 유효성 검사는 우회를 통한 요청이 올 수 있기 때문에 백엔드에서 최종 보안? 처리를 위함. 따로 프론트 응답코드로는 사용x
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ValidPasswordConfirm
public class SignUpUserRequest {

    @NotBlank(message = "블로그의 고유 ID를 입력해주세요")
    @Pattern(regexp = "^[a-z0-9-_]{3,20}$", message = "영문 소문자, 숫자, 하이픈, 언더스코어만 사용 가능합니다 (3-20자)")
    private String blogId;

    @NotBlank(message = "사용자명을 입력해주세요")
    @Size(min = 2, max = 10, message = "사용자명은 2-10자 사이여야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "사용자명은 한글(자음, 모음 불가), 영문, 숫자만 사용 가능합니다")
    private String username;

    @NotBlank(message = "이메일을 입력해주세요")
    @Pattern(
            regexp = "\\S+@\\S+\\.\\S+", // 자바에서 공백은 \s인데 공백을 올바르게 표현하기 위해서 \\S로 표현 -> 이스케이프 사용해야함.
            message = "올바른 이메일 형식이 아닙니다"
    )
    private String email;

    @ValidPassword
    private String password;

    @NotBlank(message = "패스워드를 재입력해주세요")
    private String passwordConfirm;
}
