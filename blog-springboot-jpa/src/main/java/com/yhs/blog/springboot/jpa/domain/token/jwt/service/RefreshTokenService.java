package com.yhs.blog.springboot.jpa.domain.token.jwt.service;

import com.yhs.blog.springboot.jpa.domain.token.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken findRefreshToken(String refreshToken);

    void deleteRefreshToken(Long userId);
}