package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.PostResponse;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.service.PostService;
import com.yhs.blog.springboot.jpa.service.RefreshTokenService;
import com.yhs.blog.springboot.jpa.service.UserService;
import com.yhs.blog.springboot.jpa.util.TokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenApiController {

    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final PostService postService;

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


    @GetMapping("/{id}/verify-author")
    public ResponseEntity<Map<String, Boolean>> verifyAuthor(HttpServletRequest request,
                                                             @PathVariable("id") Long postId) {

        System.out.println("실행verFiy");

        Long uesrId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);

        PostResponse postResponseDTO = postService.getPost(postId);

        boolean isAuthor = postResponseDTO.getUserId().equals(uesrId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isAuthor", isAuthor);
        return ResponseEntity.ok(response);
    }

    // MVC 방식에서 사용 할 수 있지만, REST 방식에서는 굳이 사용 하지 않음
//    @PostMapping("/new-access-token")
//    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest accessTokenRequest) {
//        String newAccessToken =
//                tokenService.createNewAccessToken(accessTokenRequest.getRefreshToken());
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateAccessTokenResponse(newAccessToken));
//    }


}
