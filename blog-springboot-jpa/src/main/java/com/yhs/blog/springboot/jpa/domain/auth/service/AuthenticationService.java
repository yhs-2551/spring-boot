package com.yhs.blog.springboot.jpa.domain.auth.service;

import com.yhs.blog.springboot.jpa.domain.auth.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


//RateLimitAspect에서 인증 실패에 관한 예외 처리 진행 
@Service
@RequiredArgsConstructor
@Log4j2
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;

    public User authenticateUser(LoginRequest loginRequest) {

        log.info("[AuthenticationService] authenticateUser() 메서드 시작");


        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);  // SecurityContextHolder에 인증 정보 저장
        return (User) authentication.getPrincipal();   // SecurityContextHolder에 저장되어 있는 사용자 주체를 꺼내서 리턴
    }


}
