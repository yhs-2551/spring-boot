package com.yhs.blog.springboot.jpa.security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public class SignUpUserResponse {
    private Long id;
    private String username;
    private String userIdentifier;
    private String email;
}
