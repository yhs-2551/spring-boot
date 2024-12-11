package com.yhs.blog.springboot.jpa.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class RateLimitResponse {
    private boolean isSuccess;
    private String message;
    private int statusCode;
    private Object data;
}
