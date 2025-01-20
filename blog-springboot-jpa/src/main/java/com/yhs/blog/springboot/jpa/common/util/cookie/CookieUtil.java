package com.yhs.blog.springboot.jpa.common.util.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.Base64;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

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

    @Loggable
    public static String serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            // 객체를 직렬화하여 바이트 배열로 변환
            objectOutputStream.writeObject(obj);

            // 직렬화된 바이트 배열을 Base64로 인코딩하여 문자열로 변환 - 객체를 직렬화한 바이트 배열은 byteArrayOutputStream에
            // 저장되어 있음
            return Base64.getUrlEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException ex) {
            throw new SystemException(ErrorCode.SERIALIZATION_ERROR, "인증 처리 중 오류가 발생했습니다. 다시 시도해 주세요.",
                    "CookieUtil", "serialize", ex);
        }
    }

    @Loggable
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {

        try {
            // 쿠키에서 Base64로 인코딩된 값을 가져와 디코딩
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie.getValue());

            // 바이트 배열을 역직렬화하여 객체로 변환
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                // 역직렬화된 객체를 원하는 타입으로 캐스팅하여 반환
                return cls.cast(objectInputStream.readObject());
            }

        } catch (IOException | ClassNotFoundException ex) {
            throw new SystemException(ErrorCode.DESERIALIZATION_ERROR, "인증 처리 중 오류가 발생했습니다. 다시 시도해 주세요.",
                    "CookieUtil", "deserialize", ex);
        }

    }

}
