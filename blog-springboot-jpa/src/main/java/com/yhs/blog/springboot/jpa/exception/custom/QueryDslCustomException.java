package com.yhs.blog.springboot.jpa.exception.custom;

import lombok.Getter;

@Getter
public class QueryDslCustomException extends RuntimeException {
    private final String errorCode;

    public QueryDslCustomException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}