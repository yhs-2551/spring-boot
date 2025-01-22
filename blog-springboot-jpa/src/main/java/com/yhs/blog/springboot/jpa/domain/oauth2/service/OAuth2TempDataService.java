package com.yhs.blog.springboot.jpa.domain.oauth2.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Service
@Log4j2
public class OAuth2TempDataService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String OAUTH2_EMAIL_PREFIX = "TEMP_OAUTH2_USER_EMAIL:";

    public String getAOAuth2UserEmail(String tempOAuth2UserUniqueId) {

        log.info("[OAuth2TempDataService] getAOAuth2UserEmail 메서드 시작");

        String key = OAUTH2_EMAIL_PREFIX + tempOAuth2UserUniqueId;

        String email = redisTemplate.opsForValue().get(key);

        if (email != null) {

            log.info("[OAuth2TempDataService] getAOAuth2UserEmail Email 값이 존재할때 분기 시작");

            redisTemplate.delete(key);
        }
        return email;
    }

}
