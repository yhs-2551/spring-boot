package com.yhs.blog.springboot.jpa.domain.token.jwt.claims;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yhs.blog.springboot.jpa.domain.token.jwt.config.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class ClaimsExtractor {

    private final JwtConfig jwtConfig;

    // 토큰 기반으로 유저 ID를 가져오는 메서드

    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    public String getSubject(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject(); // sub 클레임에서 토큰 발급자 추출
    }

    public String getBlogId(String token) {
        Claims claims = getClaims(token);
        return claims.get("blogId", String.class);
    }

    public List<String> getRoles(String token) {
        Claims claims = getClaims(token);
        List<?> rawRoles = claims.get("roles", List.class);
        return rawRoles.stream()
                .map(role -> (String) role)
                .toList();
    }

    // 페이로드, 즉 내용(클레임) 반환 메서드
    private Claims getClaims(String token) {
        try {
            return Jwts.parser().verifyWith(jwtConfig.getJwtSecretKey()).build().parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰 {}", e);
            return e.getClaims(); // 만료된 토큰에서도 클레임 반환

        }
    }
}
