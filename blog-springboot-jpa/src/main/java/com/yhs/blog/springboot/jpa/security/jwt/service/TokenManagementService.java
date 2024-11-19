package com.yhs.blog.springboot.jpa.security.jwt.service;

import com.yhs.blog.springboot.jpa.domain.token.entity.RefreshToken;
import com.yhs.blog.springboot.jpa.domain.token.repository.RefreshTokenRepository;
import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenManagementService {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = new RefreshToken(userId, newRefreshToken);

        refreshTokenRepository.save(refreshToken);

    }


    public void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse
            response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }


    public String getRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


    public void handleAccessTokenCookie(HttpServletRequest request, HttpServletResponse
            response, String accessToken) {
//        초기 로그인 시 HTTP-only 쿠키에 액세스 토큰 설정. 응답 헤더에 바로 담아서 주면 JavaScript 코드에서 이 헤더에 접근할 수 없다.
//        브라우저는 보안상의 이유로 리다이렉트 응답의 헤더를 자바스크립트에서 읽을 수 없게 하고 있기 때문이다.
//        브라우저에서 redirect가 발생할 때, 리디렉션 응답 자체의 헤더는 클라이언트에서 접근할 수 없다
//        즉, getRedirectStrategy() .sendRedirect() 메서드를 사용하면, 리디렉션된 페이지에서 응답 헤더를 클라이언트가 읽을 수 없다는
//        점을 염두에 두어야 한다.
//      액세스 토큰 HTTP Only 쿠키 저장은, 초기에 응답 헤더로 액세스 토큰을 전송해줄때만 사용하므로 setMaxAge를 60초만 설정. 지정하지 않을 수도 있음.
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true); // javascript 에서 접근 불가
        accessTokenCookie.setSecure(false); // true면 HTTPS에서만 전달, 배포 시에 true로 변경 필요
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60); // 1분 60초.

        response.addCookie(accessTokenCookie);
    }


}
