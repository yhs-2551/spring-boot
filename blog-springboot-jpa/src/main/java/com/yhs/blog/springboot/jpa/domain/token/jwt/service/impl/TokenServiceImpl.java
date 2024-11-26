package com.yhs.blog.springboot.jpa.domain.token.jwt.service.impl;

import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.impl.UserServiceImpl;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final UserServiceImpl userService;

    // 리프레시 토큰으로 토큰 유효성 검사를 진행하고, 유효한 토큰일 때 해당 리프레시 토큰을 보유하고 있는 사용자를 가져온다.
    // 이후 해당 사용자에게 새로운 Access Token을 발급한다.
    @Override
    @Transactional(readOnly = true)
    public String createNewAccessToken(String refreshToken) {

        if(!tokenProvider.validToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        Long userId = refreshTokenService.findRefreshToken(refreshToken).getUserId();
        User user = userService.findUserById(userId);
        return tokenProvider.generateToken(user, Duration.ofHours(1));
    }
}
