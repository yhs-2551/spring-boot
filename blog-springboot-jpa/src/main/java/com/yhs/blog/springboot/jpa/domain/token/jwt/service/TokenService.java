package com.yhs.blog.springboot.jpa.domain.token.jwt.service;

import com.yhs.blog.springboot.jpa.domain.user.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;

public interface TokenService {
    String createNewAccessToken(String refreshToken);

    String formLoginGenerateRefreshToken(LoginRequest loginRequest);

    String OAuth2NewUserGenerateRefreshToken(String email, User user, boolean isRememberMe);

    String formLoginGenerateAccessToken(LoginRequest loginRequest);

    String OAuth2NewUserGenerateAccessToken(User user);

}
