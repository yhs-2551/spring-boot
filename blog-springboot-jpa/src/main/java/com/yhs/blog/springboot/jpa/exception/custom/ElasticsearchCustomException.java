package com.yhs.blog.springboot.jpa.exception.custom;

import lombok.Getter;

@Getter
public class ElasticsearchCustomException extends RuntimeException {

    private final String errorCode;

    public ElasticsearchCustomException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
