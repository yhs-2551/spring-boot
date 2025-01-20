package com.yhs.blog.springboot.jpa.security.handler;

import java.io.IOException;

import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

// 로그인 과정에서 처리되는 예외. GlobalExceptionHandler에서 처리할 수 없음 
@Component
@Log4j2
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        log.error("로그인 인증 실패 예외 타입: {}", exception.getClass().getName());
        log.error("로그인 인증 실패 메시지: {}", exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "이메일 또는 비밀번호가 잘못되었습니다.",
                HttpStatus.UNAUTHORIZED.value());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }

}
