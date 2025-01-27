package com.yhs.blog.springboot.jpa.domain.auth.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.yhs.blog.springboot.jpa.common.constant.token.TokenConstants;
import com.yhs.blog.springboot.jpa.domain.auth.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.domain.auth.dto.response.LoginResultToken;
import com.yhs.blog.springboot.jpa.domain.auth.service.AuthenticationService;
import com.yhs.blog.springboot.jpa.domain.auth.service.LoginProcessService;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class LoginProcessServiceImpl implements LoginProcessService {
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;
    private final AuthenticationService authenticationService;

    @Override
    public LoginResultToken loginUser(LoginRequest loginRequest) {

        // 스프링에서 제공하는 User가 아닌 Entity 유저
        // RateLimitAspect에서 인증 실패에 관한 예외 처리 진행
        User user = authenticationService.authenticateUser(loginRequest);

        log.info("[UserServiceImpl] getTokenForLoginUser 메서드 시작");

        // 리프레시 토큰 생성
        String refreshToken;

        if (loginRequest.getRememberMe()) {

            log.info("[UserServiceImpl] getTokenForLoginUser 메서드 - RememberMe 체크한 경우 로그인 분기 시작");

            refreshToken = tokenProvider.generateToken(user,
                    TokenConstants.REMEMBER_ME_REFRESH_TOKEN_DURATION);
            redisTemplate.opsForValue().set(TokenConstants.RT_PREFIX + user.getId(),
                    refreshToken,
                    TokenConstants.REMEMBER_ME_REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        } else {

            log.info("[UserServiceImpl] getTokenForLoginUser 메서드 - RememberMe 체크하지 않은 경우 로그인 분기 시작");

            refreshToken = tokenProvider.generateToken(user, TokenConstants.REFRESH_TOKEN_DURATION);

            redisTemplate.opsForValue().set(TokenConstants.RT_PREFIX + user.getId(),
                    refreshToken,
                    TokenConstants.REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        }
        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenConstants.ACCESS_TOKEN_DURATION);
        return new LoginResultToken(refreshToken, accessToken);

    }
}
