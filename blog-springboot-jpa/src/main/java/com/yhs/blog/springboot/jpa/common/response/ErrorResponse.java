package com.yhs.blog.springboot.jpa.common.response;

import lombok.Getter;

// 에러 응답 CreatePost 부분에서 사용됨.
@Getter
public final class ErrorResponse extends ApiResponse {
    private final int errorCode;

    public ErrorResponse(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
