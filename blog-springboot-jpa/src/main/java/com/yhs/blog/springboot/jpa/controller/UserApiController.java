package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.config.jwt.TokenManagementService;
import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.AddUserRequest;
import com.yhs.blog.springboot.jpa.dto.CategoryResponse;
import com.yhs.blog.springboot.jpa.dto.LoginRequest;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.service.CategoryService;
import com.yhs.blog.springboot.jpa.service.RefreshTokenService;
import com.yhs.blog.springboot.jpa.service.TokenService;
import com.yhs.blog.springboot.jpa.service.UserService;
import com.yhs.blog.springboot.jpa.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserApiController extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenManagementService tokenManagementService;
    private final TokenService tokenService;

    private final CategoryService categoryService;


    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody AddUserRequest addUserRequest) {
        Long userId = userService.createUser(addUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws ServletException, IOException {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContextHolder에 인증 정보 저장

//        SecurityContextHolder에 저장되어 있는 사용자 주체를 꺼내옴
        User user = (User) authentication.getPrincipal();

        HttpHeaders headers = new HttpHeaders();

        String getRefreshTokenCookie = tokenManagementService.getRefreshTokenCookie(request);

        // Form로그인을 통하여 동일한 사용자가 2번이상 로그인 & 브라우저 쿠키에 리프레시 토큰이 있을 때.
        // 해당 RefreshToken을 이용해 새로운 액세스 토큰 발급
        if (getRefreshTokenCookie != null && tokenProvider.validToken(getRefreshTokenCookie)) {

            // 리프레시 토큰이 유효하다면 새로운 액세스 토큰 발급
            String newAccessToken = tokenService.createNewAccessToken(getRefreshTokenCookie);

            // 응답 헤더에 액세스 토큰 추가
            headers.set("Authorization", "Bearer " + newAccessToken);

        } else {

            // else문은 FormLogin에 초기 로그인 시

            // 리프레시 토큰 생성
            String refreshToken = tokenProvider.generateToken(user,
                    TokenManagementService.REFRESH_TOKEN_DURATION);

            // 리프레시 토큰을 userId와 함께 DB에 저장
            tokenManagementService.saveRefreshToken(user.getId(), refreshToken);

            // 생성된 리프레시 토큰을 클라이언트측 쿠키에 저장 -> 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하기 위함
            tokenManagementService.addRefreshTokenToCookie(request, response, refreshToken);

            // Access Token 생성
            String accessToken = tokenProvider.generateToken(user, TokenManagementService.ACCESS_TOKEN_DURATION);

            // 응답 헤더에 액세스 토큰 추가
            headers.set("Authorization", "Bearer " + accessToken);
        }

        //  세션이나 쿠키에 불필요한 데이터가 남아 있지 않도록 하여 보안을 강화함.
        super.clearAuthenticationAttributes(request);

        return ResponseEntity.ok().headers(headers).body("Login Success");

    }

    // Custom logout 로직을 구현한 경우 시큐리티에서 제공하는 logout을 사용하지 않는다.
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("실행 로그아웃");
        CookieUtil.deleteCookie(request, response, "refresh_token");
        CookieUtil.deleteCookie(request, response, "access_token");

        String authorizationHeader = request.getHeader("Authorization");
        // "Bearer " 이후의 토큰 값만 추출
        String token = authorizationHeader.substring(7);

        try {

            Long userId = tokenProvider.getUserId(token); // tokenProvider.gerUserId()에서 내부적으로
            // 만료된 토큰인지 유효성 검사를 함. 이때 만료된 토큰이면 아래 ExpiredJwtException Catch문으로 넘어간다.
            refreshTokenService.deleteRefreshToken(userId);
            return ResponseEntity.ok("Successfully logged out.");

        } catch (ExpiredJwtException e) {
            // 만료된 토큰일 때도 userId를 추출 가능 (ExpiredJwtException을 통해 Claims에 접근) 즉
            // ExpiredJwtException을 통해 만료된 토큰에 있는 Claims에 접근한다.
            Long userId = e.getClaims().get("id", Long.class);
            refreshTokenService.deleteRefreshToken(userId);
            return ResponseEntity.ok("Successfully logged out with expired token.");

        } catch (Exception e) {
            // 유효하지 않은 토큰(서명이 잘못되거나 변조된 경우 등 즉 비정상적인 토큰일 경우)이면 거부 한다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
        }
    }
}
