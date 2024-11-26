package com.yhs.blog.springboot.jpa.domain.token.jwt.provider;


import com.yhs.blog.springboot.jpa.domain.token.jwt.config.JwtProperties;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;


import java.util.*;
import java.util.stream.Collectors;

// 토큰 생성하고 올바른 토큰인지 유효성 검사, 토큰에서 필요한 정보를 가져오는 클래스
@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;


    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
    }

    // jwt 토큰 생성 메서드
    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        List<String> roles =
                user.getAuthorities().stream().map(authority -> authority.getAuthority()).toList();

        // 컴팩트 메서드를 사용해 최종적으로 JWT 문자열을 생성
        // 헤더 부분은 굳이 명시적으로 추가하지 않아도 된다.
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer()).issuedAt(now).expiration(expiry).subject(user.getEmail())
                .claim("id", user.getId()).claim("blogId", user.getBlogId()).claim("roles",
                        roles)
                .signWith(jwtProperties.getJwtSecretKey()).compact();
    }

    // 토큰 유효성 검사 메서드
    public boolean validToken(String token) {
        try {

            System.out.println("실행 true");
            Jwts.parser().verifyWith(jwtProperties.getJwtSecretKey()).build().parseSignedClaims(token); // JWT
            // 문자열의 토큰을 파싱하고
            // 서명을 검증한다. 유효한 경우 서명이 포함된 클레임 객체를 반환한다.
            return true;
        } catch (Exception e) {

            System.out.println("실행 false");
            System.out.println("예외 메시지: " + e.getMessage());

            return false;
        }

    }

    // 토큰 기반으로 인증 정보를 가져오는 메서드
    // 토큰을 처리하고, JWT 토큰에서 사용자 정보를 복원하는 로직.
    // 최종적으로 검증된 사용자 정보와 사용자 권한을 SecurityContext 보안 컨텍스트에 저장하기 위해 사용되는 객체
    public Authentication getAuthentication(String token) {

        Claims claims = getClaims(token);
        String blogId = claims.get("blogId", String.class);

        // 권한 설정. 바로 아래는 초기 설정
        // Set<SimpleGrantedAuthority> authorities =Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        // 권한 설정. 토큰 생성 시 지정된 사용자 ROLE을 가져와서 권한을 설정함. 0915 수정.
        List<?> rawRoles = claims.get("roles", List.class);
        List<String> roles = rawRoles.stream().map(role -> (String) role).toList();

        Set<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());

        // 사용자 주체와 사용자 권한을 토대로 User 객체 생성
        //  JWT 토큰 자체가 사용자 인증 정보를 포함하고 있기 때문에 JWT 토큰이 발급된 이후에는 패스워드가 더 이상 필요하지 않아서 빈 문자열 처리.
        org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(blogId, "", authorities);

        // 인증된 사용자 및 해당 사용자의 권한을 기반으로 UsernamePasswordAuthenticationToken 객체 생성 -> SecurityContext에
        // 저장되도록 하기 위함
        return new UsernamePasswordAuthenticationToken(user, token, authorities);

    }

//    토큰 기반으로 유저 ID를 가져오는 메서드

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


    // 페이로드, 즉 내용(클레임) 반환 메서드
    private Claims getClaims(String token) {
        try {
            return Jwts.parser().verifyWith(jwtProperties.getJwtSecretKey()).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰에서도 클레임 반환

        }
    }


}
