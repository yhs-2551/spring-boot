package com.yhs.blog.springboot.jpa.common.response;

import lombok.Getter;

@Getter
public final class SuccessResponse<T> extends ApiResponse {

    private final T data;

    public SuccessResponse(T data, String message) {
        super(message);
        this.data = data;
    }

    public SuccessResponse(String message) {
        super(message);
        this.data = null;
    }
}
