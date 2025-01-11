package com.yhs.blog.springboot.jpa.domain.token.jwt.service.impl;

import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenService;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import lombok.RequiredArgsConstructor; 
import org.springframework.stereotype.Service;

import java.time.Duration; 

@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final TokenProvider tokenProvider;
    private final UserService userService;

    // 리프레시 토큰으로 토큰 유효성 검사를 진행하고, 유효한 토큰일 때 해당 리프레시 토큰을 보유하고 있는 사용자를 가져온다.
    // 이후 해당 사용자에게 새로운 Access Token을 발급한다.
    @Override
    public String createNewAccessToken(String refreshToken) {

        Long userId = tokenProvider.getUserId(refreshToken);
        User user = userService.findUserById(userId);

        return tokenProvider.generateToken(user, Duration.ofHours(1));

    }
}
