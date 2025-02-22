package com.yhs.blog.springboot.jpa.web.cookie;

import com.yhs.blog.springboot.jpa.common.config.ApplicationContextProvider;
import com.yhs.blog.springboot.jpa.common.constant.token.TokenConstants;
import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class TokenCookieManager {

    // private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    // private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    // public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(1);
    // public static final Duration REMEMBER_ME_REFRESH_TOKEN_DURATION =
    // Duration.ofDays(14);
    // public static final long REFRESH_TOKEN_TTL = Duration.ofDays(1).toSeconds();
    // public static final long REMEMBER_ME_REFRESH_TOKEN_TTL =
    // Duration.ofDays(14).toSeconds();
    // public static final String RT_PREFIX = "RT:";

    // public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);

    public void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken,
            boolean rememberMe) {

        log.info("[TokenCookieManager] addRefreshTokenToCookie() 메서드 시작");

        int cookieMaxAge = rememberMe ? (int) TokenConstants.REMEMBER_ME_REFRESH_TOKEN_TTL
                : (int) TokenConstants.REFRESH_TOKEN_TTL;
        CookieUtil.deleteCookie(request, response, TokenConstants.REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, TokenConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    public void handleAccessTokenCookie(HttpServletRequest request, HttpServletResponse response, String accessToken) {

        log.info("[TokenCookieManager] handleAccessTokenCookie() 메서드 시작");

        // 초기 로그인 시 HTTP-only 쿠키에 액세스 토큰 설정. 리다이렉트할때 응답 헤더에 바로 담아서 주면 JavaScript 코드에서 이
        // 헤더에 접근할 수 없다.
        // 브라우저는 보안상의 이유로 리다이렉트 응답의 헤더를 자바스크립트에서 읽을 수 없게 하고 있기 때문이다.
        // 브라우저에서 redirect가 발생할 때, 리디렉션 응답 자체의 헤더는 클라이언트에서 접근할 수 없다
        // 즉, getRedirectStrategy() .sendRedirect() 메서드를 사용하면, 리디렉션된 페이지에서 응답 헤더를 클라이언트가
        // 읽을 수 없다는
        // 점을 염두에 두어야 한다.
        // 액세스 토큰 HTTP Only 쿠키 저장은, 초기에 응답 헤더로 액세스 토큰을 전송해줄때만 사용하므로 setMaxAge를 60초만 설정.
        // 지정하지 않을 수도 있음.
        Cookie accessTokenCookie = new Cookie(TokenConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken);
        accessTokenCookie.setHttpOnly(true); // javascript 에서 접근 불가, 단 HttpOnly는 클라이언트 JavaScript에서만 접근을 제한

        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60); // 1분 60초.

        if (ApplicationContextProvider.isProd()) {

            log.info("[TokenCookieManager] handleAccessTokenCookie() 메서드 isProd() 분기 진행");

            accessTokenCookie.setSecure(true);
            accessTokenCookie.setAttribute("SameSite", "None");

        }

        response.addCookie(accessTokenCookie);
    }

}
