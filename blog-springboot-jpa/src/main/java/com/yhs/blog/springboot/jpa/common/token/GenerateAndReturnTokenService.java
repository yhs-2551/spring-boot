package com.yhs.blog.springboot.jpa.common.token;

import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenCookieManager;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class GenerateAndReturnTokenService {

    private final AuthenticationService authenticationService;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public String formLoginGenerateRefreshToken(LoginRequest loginRequest) {

        User user = authenticationService.getAuthenticatedUser(loginRequest);

        String refreshToken;

        if (loginRequest.getRememberMe()) {
            refreshToken = tokenProvider.generateToken(user,
                    TokenCookieManager.REMEMBER_ME_REFRESH_TOKEN_DURATION);
            redisTemplate.opsForValue().set(TokenCookieManager.RT_PREFIX + user.getId(),
                    refreshToken,
                    TokenCookieManager.REMEMBER_ME_REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        } else {
            refreshToken = tokenProvider.generateToken(user, TokenCookieManager.REFRESH_TOKEN_DURATION);

            redisTemplate.opsForValue().set(TokenCookieManager.RT_PREFIX + user.getId(),
                    refreshToken,
                    TokenCookieManager.REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        }

        return refreshToken;

    }

    public String OAuth2NewUserGenerateRefreshToken(String email, User user, boolean isRememberMe) {
        
        // 아래는 OAuth2 신규 사용자 토큰 발급 로직
        // 리프레시 토큰 발급 및 Redis에 저장
        String refreshToken;
        if (isRememberMe) {
            refreshToken = tokenProvider.generateToken(user, TokenCookieManager.REMEMBER_ME_REFRESH_TOKEN_DURATION);
            redisTemplate.opsForValue().set(TokenCookieManager.RT_PREFIX + email, refreshToken,
                    TokenCookieManager.REMEMBER_ME_REFRESH_TOKEN_TTL, TimeUnit.SECONDS);
        } else {
            refreshToken = tokenProvider.generateToken(user, TokenCookieManager.REFRESH_TOKEN_DURATION);
            redisTemplate.opsForValue().set(TokenCookieManager.RT_PREFIX + email, refreshToken,
                    TokenCookieManager.REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        }

        return refreshToken;
    }


    public String formLoginGenerateAccessToken(LoginRequest loginRequest) {

        User user = authenticationService.getAuthenticatedUser(loginRequest);
        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenCookieManager.ACCESS_TOKEN_DURATION);
        return accessToken;

    }

    
    public String OAuth2NewUserGenerateAccessToken(User user) {

        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenCookieManager.ACCESS_TOKEN_DURATION);
        return accessToken;

    }

}
