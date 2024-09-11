package com.yhs.blog.springboot.jpa.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.IOException;

public class AuthorizationGrantTypeDeserializer extends JsonDeserializer<AuthorizationGrantType> {

    @Override
    public AuthorizationGrantType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String value = jsonParser.getText();

        // AuthorizationGrantType에 대한 기본 상수 처리
        if ("authorization_code".equals(value)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        } else if ("refresh_token".equals(value)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        }
        // 기본적인 처리
        return new AuthorizationGrantType(value);
    }
}
