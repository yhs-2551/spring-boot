package com.yhs.blog.springboot.jpa.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RateLimitResponse {
    private boolean isSuccess;
    private String message;
    private int statusCode;
    private SignUpUserResponse data;
}
