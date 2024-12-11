package com.yhs.blog.springboot.jpa.domain.user.dto.response;

import org.springframework.http.HttpHeaders;

public record SignUpResponseWithHeaders(SignUpUserResponse signUpUserResponse, HttpHeaders headers) {
}
