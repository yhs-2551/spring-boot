package com.yhs.blog.springboot.jpa.controller;


import com.yhs.blog.springboot.jpa.dto.AddUserRequest;
import com.yhs.blog.springboot.jpa.dto.LoginRequest;
import com.yhs.blog.springboot.jpa.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    SecurityContextLogoutHandler logoutHandler =
            new SecurityContextLogoutHandler();

    @PostMapping("/user")
    public ResponseEntity<Long> signup(@RequestBody AddUserRequest addUserRequest) {
        Long userId = userService.createUser(addUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContextHolder에 인증 정보 저장

        // 그냥 일반 텍스트인 '로그인 성공' 으로 응답하면 클라이언트측에서 문자열을 JSON형식으로 파싱하려다가 SyntaxError가 발생하게 된다.
        Map<String, String> response = new HashMap<>();
        response.put("message", "로그인 성공");
        return ResponseEntity.ok().body(response);

    }


    @PostMapping("/logout")
    public void logout(Authentication authentication, HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse) {
        this.logoutHandler.logout(httpServletRequest, httpServletResponse, authentication);
    }
}
