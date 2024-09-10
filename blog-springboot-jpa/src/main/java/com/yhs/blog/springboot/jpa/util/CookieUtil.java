package com.yhs.blog.springboot.jpa.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    // 요청 값(이름, 값, 만료 기간)을 바탕으로 쿠키 추가
    public static void addCookie(HttpServletResponse httpServletResponse, String name,
                                 String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // 쿠키를 HttpOnly로 설정하여 클라이언트측 JavaScript에서 접근 방지
//        cookie.setSecure(true);  쿠키가 HTTPS 연결을 통해서만 전송되도록 함.
        cookie.setMaxAge(maxAge);

        // 응답에 쿠키 추가
        httpServletResponse.addCookie(cookie);
    }

    // 쿠키의 이름을 입력받아 쿠키 삭제
    public static void deleteCookie(HttpServletRequest httpServletRequest,
                                    HttpServletResponse response, String name) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            return;
        }

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
