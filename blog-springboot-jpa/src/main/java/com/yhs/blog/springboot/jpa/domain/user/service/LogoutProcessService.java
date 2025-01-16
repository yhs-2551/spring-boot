package com.yhs.blog.springboot.jpa.domain.user.service;

import com.yhs.blog.springboot.jpa.common.constant.token.TokenConstants;
import com.yhs.blog.springboot.jpa.domain.token.jwt.claims.ClaimsExtractor;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutProcessService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ClaimsExtractor claimsExtractor;

    public void logoutUser(String token) {

        Long userId = claimsExtractor.getUserId(token);
        redisTemplate.delete(TokenConstants.RT_PREFIX + userId);
    }

    public void logoutUserByExpiredToken(ExpiredJwtException e) {
        Long userId = e.getClaims().get("id", Long.class);
        redisTemplate.delete(TokenConstants.RT_PREFIX + userId);
    }

}
