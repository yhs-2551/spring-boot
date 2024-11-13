package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.config.jwt.TokenManagementService;
import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.PostResponse;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.service.PostService;
import com.yhs.blog.springboot.jpa.service.TokenService;
import com.yhs.blog.springboot.jpa.service.UserService;
import com.yhs.blog.springboot.jpa.util.TokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenApiController {

    private final TokenProvider tokenProvider;
    private final TokenManagementService tokenManagementService;
    private final TokenService tokenService;
    private final PostService postService;
    private final UserService userService;

    @GetMapping("/initial-token")
    public ResponseEntity<String> createInitialToken(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     Authentication authentication) {

        System.out.println("다시 실행");

        String accessToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                }
            }
        }

        // 액세스 토큰이 쿠키에 없는 사용자
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not access token in " +
                    "cookie");
        }

        // 클라이언트 측 쿠키에 액세스 토큰이 있을 경우
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return ResponseEntity.ok().headers(headers).body("Initial access token retrieved and sent in headers.");

    }


    // TokenAuthenticationFilter를 통해 컨트롤러에 오기 전, 필터에서 검증
    @GetMapping("/check-access-token")
    public ResponseEntity<String> checkAccessToken(HttpServletRequest request) {

        System.out.println("체크 엑세스 토큰 실행");

        return ResponseEntity.ok("Valid Access Token ");
    }

    @PostMapping("/new-token")
    public ResponseEntity<String> createNewAccessRefreshToken(HttpServletRequest request,
                                                              HttpServletResponse response
    ) {
        HttpHeaders headers = new HttpHeaders();
        String getRefreshTokenCookie = tokenManagementService.getRefreshTokenCookie(request);
        // RefreshToken을 이용해 새로운 액세스 토큰 발급
        if (getRefreshTokenCookie != null && tokenProvider.validToken(getRefreshTokenCookie)) {
            log.info("111111111111");
            // 리프레시 토큰이 유효하다면 새로운 액세스 토큰 발급
            String newAccessToken = tokenService.createNewAccessToken(getRefreshTokenCookie);

            // 응답 헤더에 액세스 토큰 추가
            headers.set("Authorization", "Bearer " + newAccessToken);
            return ResponseEntity.ok().headers(headers).body("New access token sent in headers.");
        }

        else {
            // 리프레시 토큰까지 만료되었거나 유효하지 않은 경우. 만료된 액세스 토큰을 이용해 새롭게 리프레시 토큰 및 액세스 토큰 발급
            String autorizationHeader = request.getHeader("Authorization");
            if (autorizationHeader != null || autorizationHeader.startsWith("Bearer ")) {
                String expiredAccessToken = autorizationHeader.substring(7);
                Long userId = tokenProvider.getUserId(expiredAccessToken);
                User user = userService.findUserById(userId);

                // 리프레시 토큰 생성
                String newRefreshToken = tokenProvider.generateToken(user,
                        TokenManagementService.REFRESH_TOKEN_DURATION);

                tokenManagementService.saveRefreshToken(userId, newRefreshToken);

                tokenManagementService.addRefreshTokenToCookie(request, response, newRefreshToken);

                // Access Token 생성
                String newAccessToken = tokenProvider.generateToken(user,
                        TokenManagementService.ACCESS_TOKEN_DURATION);

                // 응답 헤더에 액세스 토큰 추가
                headers.set("Authorization", "Bearer " + newAccessToken);

            }

            return ResponseEntity.ok().headers(headers).body("New access token sent in headers " +
                    "and refresh token added in cookie.");
        }
    }

}


    // 요청 Body에 리프레시토큰을 담아서 새로운 액세스 토큰을 발급받는 로직.
//    @PostMapping("/new-access-token")
//    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest accessTokenRequest) {
//        String newAccessToken =
//                tokenService.createNewAccessToken(accessTokenRequest.getRefreshToken());
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateAccessTokenResponse(newAccessToken));
//    }


