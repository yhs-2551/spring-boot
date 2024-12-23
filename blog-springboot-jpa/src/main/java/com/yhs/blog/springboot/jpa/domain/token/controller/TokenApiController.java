package com.yhs.blog.springboot.jpa.domain.token.controller;

import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenManagementService;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenApiController {

    private final TokenProvider tokenProvider;
    private final TokenManagementService tokenManagementService;
    private final TokenService tokenService;
    private final RedisTemplate<String, String> redisTemplate;

    // OAuth2 기존 사용자의 경우
    @GetMapping("/initial-token")
    public ResponseEntity<String> createInitialToken(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     Authentication authentication) {
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("쿠키에 액세스 토큰이 존재하지 않거나 이미 액세스 토큰을 발급 받았습니다.");
        }

        String redisToken = redisTemplate.opsForValue().get("AT:" + accessToken);

        if (redisToken != null && redisToken.equals("true")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용한 액세스 토큰입니다.");
        }

        // 클라이언트 측 쿠키에 액세스 토큰이 있을 경우
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        redisTemplate.opsForValue().set("AT:" + accessToken, "true", Duration.ofMinutes(1));
        return ResponseEntity.ok().headers(headers).body("쿠키에 담긴 초기 액세스 토큰이 조회되어 새로운 액세스 토큰이 헤더에 포함되어 전송되었습니다.");

    }


    // TokenAuthenticationFilter를 통해 컨트롤러에 오기 전, 필터에서 검증
    @GetMapping("/check-access-token")
    @PreAuthorize("isAuthenticated()") // 없어도 tokenAuthenticationFilter에 의해 검증되긴 하지만, 가독성을 위해 추가
    public ResponseEntity<SuccessResponse<Void>> checkAccessToken() {

        System.out.println("체크 엑세스 토큰 실행");

        return ResponseEntity.ok(new SuccessResponse<>("액세스 토큰이 유효합니다."));
    }

    @Transactional(readOnly = true)
    @GetMapping("/new-token")
    public ResponseEntity<ApiResponse> createNewAccessRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        String getRefreshTokenCookie = tokenManagementService.getRefreshTokenCookie(request);
        String userEmail = tokenProvider.getEmail(getRefreshTokenCookie);
        // RefreshToken을 이용해 새로운 액세스 토큰 발급
        if (getRefreshTokenCookie != null && tokenProvider.validateRefreshToken(getRefreshTokenCookie, userEmail)) {
            // 리프레시 토큰이 유효하다면 새로운 액세스 토큰 발급
            String newAccessToken = tokenService.createNewAccessToken(getRefreshTokenCookie);
            // 응답 헤더에 액세스 토큰 추가
            headers.set("Authorization", "Bearer " + newAccessToken);
            return ResponseEntity.ok().headers(headers).body(new SuccessResponse<>("새로운 엑세스 토큰이 헤더에 포함되어 전송되었습니다."));
        } else {
            // 원래 이 부분 만료된 액세스 토큰으로 재발급 해줬는데, 보안측면에서 좋지 않은 설계 같아 아래처럼 처리
            // 프론트 측에서 에러 응답이면 재 로그인이 필요하다는 토스트 알림 필요
            // 레디스에 저장한 리프레시 토큰은 자동으로 삭제 되기 때문에 추가 처리 불필요. 프론트측 쿠키도 만료시간되면 브라우저에서 자동 삭제 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("세션이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED.value()));
        }
    }

}


