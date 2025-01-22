package com.yhs.blog.springboot.jpa.common.util.serialization;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import java.io.*;

import jakarta.servlet.http.Cookie;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SerializationUtils {

    @Loggable
    public static String serialize(Object obj) {

        log.info("[CookieUtil] serialize() 메서드 시작");

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

        log.info("[CookieUtil] deserialize() 메서드 시작");

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
