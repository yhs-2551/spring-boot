package com.yhs.blog.springboot.jpa.domain.token.jwt.util;

import com.yhs.blog.springboot.jpa.domain.token.jwt.claims.ClaimsExtractor;
import jakarta.servlet.http.HttpServletRequest;

public class TokenUtil {

    public static Long extractUserIdFromRequestToken(HttpServletRequest request, ClaimsExtractor claimsExtractor) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            return claimsExtractor.getUserId(accessToken);
        }

        throw new IllegalArgumentException("인증 헤더가 없거나 Bearer 로 시작하지 않습니다.");
    }

}
