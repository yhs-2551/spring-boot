package com.yhs.blog.springboot.jpa.common.util.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CookieUtil {

    // 아래 대신 WebUtils에서 제공하는 쿠키 유틸 메서드 사용하면 됨
    // public static String getCookie(HttpServletRequest request, String name) {
    // Cookie[] cookies = request.getCookies();
    // if (cookies != null) {
    // for (Cookie cookie : cookies) {
    // if (name.equals(cookie.getName())) {
    // return cookie.getValue();
    // }
    // }
    // }
    // return null;
    // }

    // 요청 값(이름, 값, 만료 기간)을 바탕으로 쿠키 추가
    public static void addCookie(HttpServletResponse httpServletResponse, String name,
            String value, int maxAge) {

        log.info("[CookieUtil] addCookie() 메서드 시작");

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // 쿠키를 HttpOnly로 설정하여 클라이언트측 JavaScript에서 접근 방지
        // cookie.setSecure(true); 쿠키가 HTTPS 연결을 통해서만 전송되도록 함.
        // cookie.setAttribute("SameSite", "Lax"); 크로스 사이트 요청 위조(CSRF) 공격을 방지하기 위한 쿠키 보안
        // 속성

        cookie.setMaxAge(maxAge);

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
                response.addCookie(cookie);
            }
        }
    }

}
