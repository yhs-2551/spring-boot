package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken findRefreshToken(String refreshToken);
}
