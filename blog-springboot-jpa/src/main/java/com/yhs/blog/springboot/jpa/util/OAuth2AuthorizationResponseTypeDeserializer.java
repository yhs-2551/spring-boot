package com.yhs.blog.springboot.jpa.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;

import java.io.IOException;

public class OAuth2AuthorizationResponseTypeDeserializer extends JsonDeserializer<OAuth2AuthorizationResponseType> {
    @Override
    public OAuth2AuthorizationResponseType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String value = node.get("value").asText();

        if("code".equals(value)) {
            return OAuth2AuthorizationResponseType.CODE;
        }

        return new OAuth2AuthorizationResponseType(value);
    }
}
