package com.yhs.blog.springboot.jpa.domain.auth.token.service;
 
public interface TokenService {
    String createNewAccessToken(String refreshToken);
}
