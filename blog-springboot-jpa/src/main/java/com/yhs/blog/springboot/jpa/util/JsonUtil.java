package com.yhs.blog.springboot.jpa.util;

import jakarta.servlet.http.Cookie;
import java.io.*;
import java.util.Base64;

public class JsonUtil {

    public static String serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            // 객체를 직렬화하여 바이트 배열로 변환
            objectOutputStream.writeObject(obj);

            // 바이트 배열을 Base64로 인코딩하여 문자열로 변환
            return Base64.getUrlEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException("Serialization Error", ex);
        }
    }


    public static <T> T deserialize(Cookie cookie, Class<T> cls) {

        try {
            // 쿠키에서 Base64로 인코딩된 값을 가져와 디코딩
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie.getValue());

            // 바이트 배열을 역직렬화하여 객체로 변환
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
                 ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
            ) {
                // 역직렬화된 객체를 원하는 타입으로 캐스팅하여 반환
                return cls.cast(objectInputStream.readObject());
            }

        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException("Deserialization error", ex);
        }

    }

}
