package com.yhs.blog.springboot.jpa.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public class SignUpUserResponse {
    private Long id;
    private String blogId;
    private String userName;
    private String email;
}
