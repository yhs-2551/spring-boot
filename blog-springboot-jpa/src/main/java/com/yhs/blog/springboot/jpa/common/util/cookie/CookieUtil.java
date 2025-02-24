package com.yhs.blog.springboot.jpa.common.util.cookie;

import com.yhs.blog.springboot.jpa.common.config.ApplicationContextProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CookieUtil {

    // getCookie는 Spring에서 제공해주는 기능 사용

    // public static String getCookie(HttpServletRequest httpServletRequest, String name) {

    //     log.info("[CookieUtil] getCookie() 메서드 시작");

    //     Cookie[] cookies = httpServletRequest.getCookies();
    //     if (cookies == null) {

    //         log.info("[CookieUtil] getCookie() 메서드 cookies == null 분기 진행");

    //         return null;
    //     }

    //     log.info("[CookieUtil] getCookie() 메서드 cookies != null 분기 진행");

    //     for (Cookie cookie : cookies) {
    //         if (name.equals(cookie.getName())) {
    //             return cookie.getValue();
    //         }
    //     }

    //     log.info("[CookieUtil] getCookie() 메서드 일치하는 쿠키가 없을 때 null 반환");

    //     return null;
    // }

    // 요청 값(이름, 값, 만료 기간)을 바탕으로 쿠키 추가
    public static void addCookie(HttpServletResponse httpServletResponse, String name,
            String value, int maxAge) {

        log.info("[CookieUtil] addCookie() 메서드 시작");

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // 쿠키를 HttpOnly로 설정하여 클라이언트측 JavaScript에서 접근 방지
        cookie.setMaxAge(maxAge);

        if (ApplicationContextProvider.isProd()) {

            log.info("[CookieUtil] addCookie() 메서드 isProd() 분기 진행");

            cookie.setSecure(true); // 쿠키가 HTTPS 연결을 통해서만 전송되도록 함. samesite = none 일 때 필수
            cookie.setAttribute("SameSite", "None"); // 크로스 도메인 요청에서 쿠키 전송 가능. 서로 다른 도메인 일때만 설정
            // cookie.setDomain("dduha.duckdns.org"); // setDomain은 duckdns, github pages와
            // 같은 공용(퍼블릭) 도메인 에서는 보안상 설정 불가능

        }
        // 응답에 쿠키 추가
        httpServletResponse.addCookie(cookie);
    }

    // 쿠키의 이름을 입력받아 쿠키 삭제
    public static void deleteCookie(HttpServletRequest httpServletRequest,
            HttpServletResponse response, String name) {

        log.info("[CookieUtil] deleteCookie() 메서드 시작");

        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {

            log.info("[CookieUtil] deleteCookie() 메서드 시작 cookies == null 분기 진행");

            return;
        }

        log.info("[CookieUtil] deleteCookie() 메서드 시작 cookies != null 분기 진행");

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);

                if (ApplicationContextProvider.isProd()) {

                    log.info("[CookieUtil] deleteCookie() 메서드 isProd() 분기 진행");

                    cookie.setSecure(true);
                    cookie.setAttribute("SameSite", "None");

                }

                response.addCookie(cookie);
            }
        }
    }

}
