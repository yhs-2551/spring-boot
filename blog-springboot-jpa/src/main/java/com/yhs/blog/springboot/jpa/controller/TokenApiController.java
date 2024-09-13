package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.CreateAccessTokenRequest;
import com.yhs.blog.springboot.jpa.dto.CreateAccessTokenResponse;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.service.TokenService;

import com.yhs.blog.springboot.jpa.service.impl.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenApiController {

    private final TokenService tokenService;
    private final TokenProvider tokenProvider;

    @GetMapping("/initial-token")
    // ResponseEntity에 CreateAccessTokenResponse와 문자열 둘 다 들어가기 때문에 Object로 사용
    public ResponseEntity<Object> createInitialToken(HttpServletRequest request,
                                                     HttpServletResponse response) {
        String accessToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        if (accessToken == null) {
            return ResponseEntity.status(401).body("Access Token not found in cookies");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        return ResponseEntity.ok().headers(headers).body("Initial access token retrieved and sent in headers.");
    }


    @GetMapping("/check-access-token")
    public ResponseEntity<Object> checkAccessToken(HttpServletRequest request) {

        System.out.println("토큰 체크 실행");


        String authorizationHeader = request.getHeader("Authorization");


        // Authorization 헤더가 없는 경우 401 반환
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token is missing");
        }


        // "Bearer " 이후의 토큰 값만 추출
        String token = authorizationHeader.substring(7);

        System.out.println("token값>>>" + token);

        // 토큰 유효성 검증
        if (!tokenProvider.validToken(token)) {
            System.out.println("유효하지 않은 토큰 체크 실행");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired access token");
        }

        System.out.println("유효한 토큰 체크 실행");

//        액세스 토큰 검증이 완료되면 유효한 토큰 이라고 body에 담기만 하면 되고, user 정보를 굳이 넘겨줄 필요 없어서 아래는 주석 처리
//        Long userId = tokenProvider.getUserId(token);
//        System.out.println("userid 값 >>>" + userId);
//        User user = userService.findUserById(userId);

        return ResponseEntity.ok("Valid Access Token ");


    }


    // 아마 사용 안함
    @PostMapping("/new-access-token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest accessTokenRequest) {
        String newAccessToken =
                tokenService.createNewAccessToken(accessTokenRequest.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateAccessTokenResponse(newAccessToken));
    }

}
