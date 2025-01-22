package com.yhs.blog.springboot.jpa.domain.token.jwt.service.impl;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.token.jwt.claims.ClaimsExtractor;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenService;
import com.yhs.blog.springboot.jpa.domain.token.jwt.validation.TokenValidator;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
@Log4j2
public class TokenServiceImpl implements TokenService {

    private final TokenProvider tokenProvider;
    private final TokenValidator tokenValidator;
    private final ClaimsExtractor claimsExtractor;
    private final UserService userService;

    @Override
    @Loggable
    public String createNewAccessToken(String refreshToken) {

        log.info("[TokenServiceImpl] createNewAccessToken 메서드 시작");

        Long userId = claimsExtractor.getUserId(refreshToken);

        if (userId != null // Long은 wrapper 클래스라 null 비교 가능
                && tokenValidator.validateRefreshToken(refreshToken, userId)) {

            log.info("[TokenServiceImpl] createNewAccessToken RefreshToken 유효성 검사 통과 완료 분기");

            User user = userService.findUserById(userId);

            return tokenProvider.generateToken(user, Duration.ofHours(1));

        } else {

            throw new BusinessException(
                    ErrorCode.REFRESH_TOKEN_EXPIRED,
                    "세션이 만료되었습니다. 다시 로그인해주세요.",
                    "TokenServiceImpl",
                    "createNewAccessToken");

        }

    }

}
