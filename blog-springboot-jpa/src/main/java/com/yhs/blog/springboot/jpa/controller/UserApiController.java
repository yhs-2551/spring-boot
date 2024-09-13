package com.yhs.blog.springboot.jpa.controller;


import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.AddUserRequest;
import com.yhs.blog.springboot.jpa.dto.LoginRequest;
import com.yhs.blog.springboot.jpa.repository.RefreshTokenRepository;
import com.yhs.blog.springboot.jpa.service.RefreshTokenService;
import com.yhs.blog.springboot.jpa.service.UserService;
import com.yhs.blog.springboot.jpa.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserApiController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final TokenProvider tokenProvider;

    private final RefreshTokenService refreshTokenService;

//    SecurityContextLogoutHandler logoutHandler =
//            new SecurityContextLogoutHandler();

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody AddUserRequest addUserRequest) {
        Long userId = userService.createUser(addUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest
            request) {

        System.out.println("run login");

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContextHolder에 인증 정보 저장

        // 세션 생성 및 사용자 정보 저장. JWT를 사용할 경우 불필요
//        HttpSession session = request.getSession(true);
//        session.setAttribute("username", loginRequest.getEmail());

        // 그냥 일반 텍스트인 '로그인 성공' 으로 응답하면 클라이언트측에서 문자열을 JSON형식으로 파싱하려다가 SyntaxError가 발생하게 된다.
        Map<String, String> response = new HashMap<>();
        response.put("message", "로그인 성공");
        return ResponseEntity.ok().body(response);

    }


//    스프링 시큐리티에서 기본적으로 세션을 무효화하고 쿠키를 삭제하는 로직. (세션 방식)
//    @PostMapping("/logout")
//    public void logout(Authentication authentication, HttpServletRequest httpServletRequest,
//                       HttpServletResponse httpServletResponse) {
//        this.logoutHandler.logout(httpServletRequest, httpServletResponse, authentication);
//
//        System.out.println("로그아웃 실행");
//    }


    // Custom logout 로직을 구현한 경우 시큐리티에서 제공하는 logout을 사용하지 않는다.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {


        CookieUtil.deleteCookie(request, response, "refresh_token");
        CookieUtil.deleteCookie(request, response, "access_token");

        System.out.println("logout controller execute");

        String authorizationHeader = request.getHeader("Authorization");
        // "Bearer " 이후의 토큰 값만 추출
        String token = authorizationHeader.substring(7);

        Long id = tokenProvider.getUserId(token);

        System.out.println("log out user id >>>" + id);

        refreshTokenService.deleteRefreshToken(id);
        return ResponseEntity.ok().build();
    }
}
