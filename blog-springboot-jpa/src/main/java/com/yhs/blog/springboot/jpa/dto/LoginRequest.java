package com.yhs.blog.springboot.jpa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
}
