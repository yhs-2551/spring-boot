package com.yhs.blog.springboot.jpa.exception.custom;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}