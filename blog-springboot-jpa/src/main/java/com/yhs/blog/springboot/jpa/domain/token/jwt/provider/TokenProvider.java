package com.yhs.blog.springboot.jpa.domain.token.jwt.provider;

import com.yhs.blog.springboot.jpa.domain.token.jwt.config.JwtConfig;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenCookieManager;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;

// 토큰 생성하고 올바른 토큰인지 유효성 검사, 토큰에서 필요한 정보를 가져오는 클래스
@RequiredArgsConstructor
@Service
@Log4j2
public class TokenProvider {

    private final JwtConfig jwtConfig;

    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
    }

    // jwt 토큰 생성 메서드
    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        List<String> roles = user.getAuthorities().stream().map(authority -> authority.getAuthority()).toList();

        // 컴팩트 메서드를 사용해 최종적으로 JWT 문자열을 생성
        // 헤더 부분은 굳이 명시적으로 추가하지 않아도 된다.
        return Jwts.builder()
                .issuer(jwtConfig.getIssuer()).issuedAt(now).expiration(expiry).subject(String.valueOf(user.getId()))
                .claim("id", user.getId())
                .claim("blogId", user.getBlogId())
                .claim("username", user.getUsername())
                .claim("roles",
                        roles)
                .signWith(jwtConfig.getJwtSecretKey()).compact();
    }

}
