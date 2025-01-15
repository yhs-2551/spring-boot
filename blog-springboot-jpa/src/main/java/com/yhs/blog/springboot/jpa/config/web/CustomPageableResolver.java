package com.yhs.blog.springboot.jpa.config.web;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class CustomPageableResolver extends PageableHandlerMethodArgumentResolver {
    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        // 원본 Pageable 객체 생성
        Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        // 새로운 PageRequest 객체 반환 (1-based → 0-based 변환)
        // page 파라미터가 없는 경우 pageable.getPageNumber()는 0 반환
        return PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1), // 0밑으로 내려가지 않도록 Math.max 사용
                pageable.getPageSize(),
                pageable.getSort());
    }
}