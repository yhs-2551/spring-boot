package com.yhs.blog.springboot.jpa.security.jwt.util;

import com.yhs.blog.springboot.jpa.security.jwt.provider.TokenProvider;
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

//    public static String extractEmailFromRequestToken(HttpServletRequest request, TokenProvider tokenProvider) {
//        String authorizationHeader = request.getHeader("Authorization");
//
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            String accessToken = authorizationHeader.substring(7);
//            return tokenProvider.getEmailFromToken(accessToken);
//        }
//
//        throw new IllegalArgumentException("Invalid or missing Authorization header");
//    }

//    public static String extractUserIdentifierFromRequestToken(HttpServletRequest request, TokenProvider tokenProvider) {
//        String authorizationHeader = request.getHeader("Authorization");
//
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            String accessToken = authorizationHeader.substring(7);
//            return tokenProvider.getUserIdentifier(accessToken);
//        }
//
//        throw new IllegalArgumentException("Invalid or missing Authorization header");
//    }
}
