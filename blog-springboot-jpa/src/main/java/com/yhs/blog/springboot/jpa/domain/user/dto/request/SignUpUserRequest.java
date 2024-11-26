package com.yhs.blog.springboot.jpa.domain.user.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SignUpUserRequest {
    private String blogId;
    private String username;
    private String email;
    private String password;
}
