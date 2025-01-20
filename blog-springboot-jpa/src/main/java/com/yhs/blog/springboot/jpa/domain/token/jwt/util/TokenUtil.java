package com.yhs.blog.springboot.jpa.domain.token.jwt.util;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.token.jwt.claims.ClaimsExtractor;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;

import jakarta.servlet.http.HttpServletRequest;

public class TokenUtil {

    @Loggable
    public static Long extractUserIdFromRequestToken(HttpServletRequest request, ClaimsExtractor claimsExtractor) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            return claimsExtractor.getUserId(accessToken);
        }

        throw new BusinessException(
                ErrorCode.ACCESS_TOKEN_EMPTY,
                "액세스 토큰이 누락 되었습니다.",
                "TokenUtil",
                "extractUserIdFromRequestToken");
    }

}
