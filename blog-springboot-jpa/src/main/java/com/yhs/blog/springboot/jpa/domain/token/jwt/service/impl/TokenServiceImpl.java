package com.yhs.blog.springboot.jpa.domain.token.jwt.service.impl;

import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenService;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.exception.custom.UnauthorizedException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final TokenProvider tokenProvider;
    private final UserService userService;

    @Override
    public String createNewAccessToken(String refreshToken) {

        Long userId = tokenProvider.getUserId(refreshToken);

        if (userId != null // Long은 wrapper 클래스라 null 비교 가능
                && tokenProvider.validateRefreshToken(refreshToken, userId)) {
            //
            User user = userService.findUserById(userId);

            return tokenProvider.generateToken(user, Duration.ofHours(1));

        } else {

            throw new UnauthorizedException("세션이 만료되었습니다. 다시 로그인해주세요.");
        }

    }
}
