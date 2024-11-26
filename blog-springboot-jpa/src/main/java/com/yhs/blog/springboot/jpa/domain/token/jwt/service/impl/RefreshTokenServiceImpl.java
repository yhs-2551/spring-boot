package com.yhs.blog.springboot.jpa.domain.token.jwt.service.impl;

import com.yhs.blog.springboot.jpa.domain.token.entity.RefreshToken;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.RefreshTokenService;
import com.yhs.blog.springboot.jpa.domain.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken findRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new IllegalArgumentException("Refresh Token not found"));
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }
}
