package com.yhs.blog.springboot.jpa.domain.user.service;

import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenManagementService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutProcessService {

    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public void logoutUser(String token) {

        Long userId = tokenProvider.getUserId(token);
        redisTemplate.delete(TokenManagementService.RT_PREFIX + userId);
    }

    public void logoutUserByExpiredToken(ExpiredJwtException e) {
        Long userId = e.getClaims().get("id", Long.class);
        redisTemplate.delete(TokenManagementService.RT_PREFIX + userId);
    }

}
