package com.yhs.blog.springboot.jpa.domain.auth.service.impl;

import com.yhs.blog.springboot.jpa.common.constant.token.TokenConstants;
import com.yhs.blog.springboot.jpa.domain.auth.service.LogoutProcessService;
import com.yhs.blog.springboot.jpa.domain.auth.token.claims.ClaimsExtractor;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class LogoutProcessServiceImpl implements LogoutProcessService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ClaimsExtractor claimsExtractor;

    @Override
    public void logoutUser(String token) {

        log.info("[LogoutProcessService] logoutUser() 메서드 시작");

        Long userId = claimsExtractor.getUserId(token);
        redisTemplate.delete(TokenConstants.RT_PREFIX + userId);
    }

    @Override
    public void logoutUserByExpiredToken(ExpiredJwtException e) {

        log.info("[LogoutProcessService] logoutUserByExpiredToken() 메서드 시작");

        Long userId = e.getClaims().get("id", Long.class);
        redisTemplate.delete(TokenConstants.RT_PREFIX + userId);
    }

}
