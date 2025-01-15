package com.yhs.blog.springboot.jpa.domain.oauth2.dto.request;

import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;

public record OAuth2SignUpResponse(
        SignUpUserResponse userInfo,
        String refreshToken,
        String accessToken,
        boolean isRememberMe) {
}
