package com.yhs.blog.springboot.jpa.security.jwt.service;

public interface TokenService {
    String createNewAccessToken(String refreshToken);
}
