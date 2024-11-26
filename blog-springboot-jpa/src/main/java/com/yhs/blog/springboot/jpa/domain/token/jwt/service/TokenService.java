package com.yhs.blog.springboot.jpa.domain.token.jwt.service;

public interface TokenService {
    String createNewAccessToken(String refreshToken);
}
