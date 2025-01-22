package com.yhs.blog.springboot.jpa.domain.token.jwt.validation;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.yhs.blog.springboot.jpa.common.constant.token.TokenConstants;
import com.yhs.blog.springboot.jpa.domain.token.jwt.config.JwtConfig;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class TokenValidator {

    private final JwtConfig jwtConfig;

    private final RedisTemplate<String, String> redisTemplate;

    // 토큰 유효성 검사 메서드
    public boolean validateAccessToken(String token) {

        log.info("[TokenValidator] validateAccessToken() 메서드 시작");

        try {
            Jwts.parser().verifyWith(jwtConfig.getJwtSecretKey()).build().parseSignedClaims(token); // JWT
            // 문자열의 토큰을 파싱하고
            // 서명을 검증한다. 유효한 경우 서명이 포함된 클레임 객체를 반환한다.
            log.info("[TokenValidator] validateAccessToken() 메서드 액세스 토큰 검증 성공");
            return true;
        } catch (Exception e) {
            log.error("[TokenValidator] validateAccessToken() 메서드 액세스 토큰 검증 실패 에러: {}", e.getMessage());
            return false;
        }

    }

    public boolean validateRefreshToken(String token, Long userId) {

        log.info("[TokenValidator] validateRefreshToken() 메서드 시작");

        String storedRefreshToken = redisTemplate.opsForValue().get(TokenConstants.RT_PREFIX + userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(token)) {

            log.error("[TokenValidator] validateRefreshToken() 메서드 리프레시 토큰 검증 실패(Redis에 저장된 토큰과 일치하지 않음)");

            return false;
        }

        try {
            Jwts.parser().verifyWith(jwtConfig.getJwtSecretKey()).build().parseSignedClaims(token); // JWT
            // 문자열의 토큰을 파싱하고
            // 서명을 검증한다. 유효한 경우 서명이 포함된 클레임 객체를 반환한다.
            log.info("[TokenValidator] validateRefreshToken() 메서드 리프레시 토큰 검증 성공");
            return true;
        } catch (Exception e) {
            log.error("[TokenValidator] validateRefreshToken() 메서드 리프레시 토큰 검증 실패 에러: {}", e.getMessage());
            return false;
        }
    }
}
