package com.yhs.blog.springboot.jpa.security.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SignUpUserRequest {
    private String username;
    private String email;
    private String password;
}
