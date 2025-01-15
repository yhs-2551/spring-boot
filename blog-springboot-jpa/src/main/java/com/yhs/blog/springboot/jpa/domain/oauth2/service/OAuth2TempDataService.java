package com.yhs.blog.springboot.jpa.domain.oauth2.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OAuth2TempDataService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String OAUTH2_EMAIL_PREFIX = "TEMP_OAUTH2_USER_EMAIL";

    public String getAOAuth2UserEmail(String tempOAuth2UserUniqueId) {
        String key = OAUTH2_EMAIL_PREFIX + tempOAuth2UserUniqueId;
        String email = redisTemplate.opsForValue().get(key);
        if (email != null) {
            redisTemplate.delete(key);
        }
        return email;
    }

}
