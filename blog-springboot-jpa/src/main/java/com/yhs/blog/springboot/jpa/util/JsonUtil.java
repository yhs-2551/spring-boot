package com.yhs.blog.springboot.jpa.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public static <T> T deserialize(String base64String, Class<T> cls) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(base64String);
            String jsonString = new String(decodedBytes);
            return objectMapper.readValue(jsonString, cls);
        } catch (Exception ex) {
            throw new RuntimeException("Deserialization error", ex);
        }
    }

}
