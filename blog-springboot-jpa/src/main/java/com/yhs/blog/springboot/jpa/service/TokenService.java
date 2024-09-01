package com.yhs.blog.springboot.jpa.service;

public interface TokenService {
    String createNewAccessToken(String refreshToken);
}
