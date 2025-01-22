package com.yhs.blog.springboot.jpa.domain.oauth2.dto.request;

public record OAuth2SignUpResponse(
        String refreshToken,
        String accessToken,
        boolean isRememberMe) {
}
