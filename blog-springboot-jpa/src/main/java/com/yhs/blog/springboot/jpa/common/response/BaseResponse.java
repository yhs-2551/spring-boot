package com.yhs.blog.springboot.jpa.common.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

//인스턴스화 불가, 상속하는 클래스들만 인스턴스화할 수 있다. 공통된 기능만 정의
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
public sealed abstract class BaseResponse permits SuccessResponse, ErrorResponse {

    private final String message;

    protected BaseResponse(String message) {
        this.message = message;
    }
}
