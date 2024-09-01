package com.yhs.blog.springboot.jpa.service.impl;

import com.yhs.blog.springboot.jpa.entity.RefreshToken;
import com.yhs.blog.springboot.jpa.repository.RefreshTokenRepository;
import com.yhs.blog.springboot.jpa.service.RefreshTokenService;
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
}
