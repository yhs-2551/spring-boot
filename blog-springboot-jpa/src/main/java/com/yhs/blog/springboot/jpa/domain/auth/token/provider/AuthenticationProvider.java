package com.yhs.blog.springboot.jpa.domain.auth.token.provider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.yhs.blog.springboot.jpa.domain.auth.token.claims.ClaimsExtractor;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class AuthenticationProvider {

    private final ClaimsExtractor claimsExtractor;

    // 토큰 기반으로 인증 정보를 가져오는 메서드
    // 토큰을 처리하고, JWT 토큰에서 사용자 정보를 복원하는 로직.
    // 최종적으로 검증된 사용자 정보와 사용자 권한을 SecurityContext 보안 컨텍스트에 저장하기 위해 사용되는 객체
    public Authentication getAuthentication(String token) {

        log.info("[AuthenticationProvider] getAuthentication() 메서드 시작");

        String blogId = claimsExtractor.getBlogId(token);

        // 권한 설정. 바로 아래는 초기 설정
        // Set<SimpleGrantedAuthority> authorities =Collections.singleton(new
        // SimpleGrantedAuthority("ROLE_USER"));

        // 권한 설정. 토큰 생성 시 지정된 사용자 ROLE을 가져와서 권한을 설정함. 0915 수정.
        List<String> roles = claimsExtractor.getRoles(token);

        Set<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        // 사용자 주체와 사용자 권한을 토대로 User 객체 생성
        // JWT 토큰 자체가 사용자 인증 정보를 포함하고 있기 때문에 JWT 토큰이 발급된 이후에는 패스워드가 더 이상 필요하지 않아서 빈 문자열
        // 처리.
        UserDetails user = new User(
                blogId, "", authorities);

        // 인증된 사용자 및 해당 사용자의 권한을 기반으로 UsernamePasswordAuthenticationToken 객체 생성 ->
        // SecurityContext에
        // 저장되도록 하기 위함
        return new UsernamePasswordAuthenticationToken(user, token, authorities);

    }

}
