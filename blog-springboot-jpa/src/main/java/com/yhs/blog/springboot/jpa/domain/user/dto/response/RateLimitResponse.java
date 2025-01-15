package com.yhs.blog.springboot.jpa.domain.user.dto.response;

import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class RateLimitResponse<T> {
    private boolean isSuccess;
    @Nullable
    private String message;
    @Nullable
    private Integer statusCode;
    @Nullable
    private T data;

    // createOAuth2User 메서드에서 사용
    public RateLimitResponse(boolean success, T data) {
        this.isSuccess = success;
        this.message = null;
        this.statusCode = null;
        this.data = data;
    }

}
