package com.yhs.blog.springboot.jpa.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.servlet.http.Cookie;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 객체를 JSON으로 직렬화하고 Base64로 인코딩
    public static String serialize(Object obj) {
        try {
            String jsonString = objectMapper.writeValueAsString(obj);
            return Base64.getUrlEncoder().encodeToString(jsonString.getBytes());
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Serialization Error", ex);
        }
    }

    // Base64로 인코딩된 JSON 문자열을 객체로 역직렬화
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {

        try {
//            System.out.println("Base64 String: " + base64String);
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie.getValue());
            System.out.println("decodedBytes: " + decodedBytes);
            String jsonString = new String(decodedBytes);
            System.out.println("Decoded JSON String: " + jsonString);
            return objectMapper.readValue(jsonString, cls);
        } catch (Exception ex) {
            throw new RuntimeException("Deserialization error", ex);
        }

    }

}
