package com.yhs.blog.springboot.jpa.domain.token.jwt.service.impl;

import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenService;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final TokenProvider tokenProvider;
    private final UserServiceImpl userService;

    // 리프레시 토큰으로 토큰 유효성 검사를 진행하고, 유효한 토큰일 때 해당 리프레시 토큰을 보유하고 있는 사용자를 가져온다.
    // 이후 해당 사용자에게 새로운 Access Token을 발급한다.
    @Override
    public String createNewAccessToken(String refreshToken) {

        String userEmail = tokenProvider.getEmail(refreshToken);
        Optional<User> user = userService.findUserByEmail(userEmail);

        if (user.isPresent()) {
            return tokenProvider.generateToken(user.get(), Duration.ofHours(1));
        } else {
            throw new UsernameNotFoundException("User not found with email: " + userEmail);
        }

    }
}
