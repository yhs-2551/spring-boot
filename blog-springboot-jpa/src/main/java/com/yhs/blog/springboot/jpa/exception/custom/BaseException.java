package com.yhs.blog.springboot.jpa.exception.custom;

import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;

import lombok.Getter;

@Getter
public sealed abstract class BaseException extends RuntimeException permits SystemException, BusinessException {
    private final ErrorCode errorCode;
    private final String className;
    private final String methodName;

    public BaseException(ErrorCode errorCode, String message,
            String className, String methodName) {
        super(message);
        this.errorCode = errorCode;
        this.className = className;
        this.methodName = methodName;
    }

    public BaseException(ErrorCode errorCode, String message,
            String className, String methodName, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.className = className;
        this.methodName = methodName;
    }
}
