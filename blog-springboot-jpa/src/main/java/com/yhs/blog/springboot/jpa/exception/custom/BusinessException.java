package com.yhs.blog.springboot.jpa.exception.custom;

import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;

public final class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode, String message, String className, String methodName) {
        super(errorCode, message, className, methodName);
    }

    public BusinessException(ErrorCode errorCode, String message, String className, String methodName,
            Throwable cause) {
        super(errorCode, message, className, methodName, cause);
    }
}
