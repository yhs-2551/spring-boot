package com.yhs.blog.springboot.jpa.util;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;

public class TokenUtil {

    public static Long extractUserIdFromRequestToken(HttpServletRequest request, TokenProvider tokenProvider) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            return tokenProvider.getUserId(accessToken);
        }

        throw new IllegalArgumentException("Invalid or missing Authorization header");
    }
}
