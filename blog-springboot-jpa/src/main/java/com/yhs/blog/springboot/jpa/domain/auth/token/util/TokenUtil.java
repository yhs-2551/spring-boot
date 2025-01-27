package com.yhs.blog.springboot.jpa.domain.auth.token.util;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.auth.token.claims.ClaimsExtractor;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TokenUtil {

    @Loggable
    public static Long extractUserIdFromRequestToken(HttpServletRequest request, ClaimsExtractor claimsExtractor) {

        log.info("[TokenUtil] extractUserIdFromRequestToken 메서드 시작");

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            log.info("[TokenUtil] extractUserIdFromRequestToken Authorization 헤더 존재 분기 진행");

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
