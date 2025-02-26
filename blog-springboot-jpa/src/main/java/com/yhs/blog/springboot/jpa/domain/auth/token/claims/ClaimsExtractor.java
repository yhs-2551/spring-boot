package com.yhs.blog.springboot.jpa.domain.auth.token.claims;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yhs.blog.springboot.jpa.domain.auth.token.config.JwtConfig;

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

        log.info("[ClaimsExtractor] getUserId() 메서드 시작");

        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    public String getSubject(String token) {

        log.info("[ClaimsExtractor] getSubject() 메서드 시작");

        Claims claims = getClaims(token);
        return claims.getSubject(); // sub 클레임에서 토큰 발급자 추출
    }

    public String getBlogId(String token) {

        log.info("[ClaimsExtractor] getBlogId() 메서드 시작");

        Claims claims = getClaims(token);
        return claims.get("blogId", String.class);

    }

    public String extractBlogIdFromExpiredToken(String token) {

        log.info("[ClaimsExtractor] getBlogId() 메서드 시작");

        try {

            Claims claims = getClaims(token);
            return claims.get("blogId", String.class);
        } catch (ExpiredJwtException e) {

            log.info("[ClaimsExtractor] extractBlogIdFromExpiredToken() 메서드 만료된 토큰에서 블로그 ID 추출");

            return e.getClaims().get("blogId", String.class);

        }

    }

    public String getUsername(String token) {

        log.info("[ClaimsExtractor] getUsername() 메서드 시작");

        Claims claims = getClaims(token);
        return claims.get("username", String.class);
    }

    public List<String> getRoles(String token) {

        log.info("[ClaimsExtractor] getRoles() 메서드 시작");

        Claims claims = getClaims(token);
        List<?> rawRoles = claims.get("roles", List.class);
        return rawRoles.stream()
                .map(role -> (String) role)
                .toList();
    }

    // 페이로드, 즉 내용(클레임) 반환 메서드
    private Claims getClaims(String token) {

        log.info("[ClaimsExtractor] getClaims() 메서드 시작");

        return Jwts.parser().verifyWith(jwtConfig.getJwtSecretKey()).build().parseSignedClaims(token)
                .getPayload();

    }
}
