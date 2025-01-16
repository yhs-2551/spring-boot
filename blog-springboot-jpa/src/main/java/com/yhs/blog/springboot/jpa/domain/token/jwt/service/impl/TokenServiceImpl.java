package com.yhs.blog.springboot.jpa.domain.token.jwt.service.impl;

import com.yhs.blog.springboot.jpa.domain.token.jwt.claims.ClaimsExtractor;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenCookieManager;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenService;
import com.yhs.blog.springboot.jpa.domain.token.jwt.validation.TokenValidator;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.AuthenticationService;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.exception.custom.UnauthorizedException;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.LoginRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final TokenProvider tokenProvider;
    private final TokenValidator tokenValidator;
    private final ClaimsExtractor claimsExtractor;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String createNewAccessToken(String refreshToken) {

        Long userId = claimsExtractor.getUserId(refreshToken);

        if (userId != null // Long은 wrapper 클래스라 null 비교 가능
                && tokenValidator.validateRefreshToken(refreshToken, userId)) {
            //
            User user = userService.findUserById(userId);

            return tokenProvider.generateToken(user, Duration.ofHours(1));

        } else {

            throw new UnauthorizedException("세션이 만료되었습니다. 다시 로그인해주세요.");
        }

    }


    @Override
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


    @Override
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


    @Override
    public String formLoginGenerateAccessToken(LoginRequest loginRequest) {

        User user = authenticationService.getAuthenticatedUser(loginRequest);
        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenCookieManager.ACCESS_TOKEN_DURATION);
        return accessToken;

    }

    @Override
    public String OAuth2NewUserGenerateAccessToken(User user) {

        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenCookieManager.ACCESS_TOKEN_DURATION);
        return accessToken;

    }

}
