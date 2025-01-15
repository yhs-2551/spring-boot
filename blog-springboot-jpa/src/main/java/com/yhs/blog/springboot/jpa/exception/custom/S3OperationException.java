package com.yhs.blog.springboot.jpa.exception.custom;

public class S3OperationException extends RuntimeException{
    public S3OperationException(String message,  Throwable cause) {
        super(message, cause);
    }
}
