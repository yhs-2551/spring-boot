package com.yhs.blog.springboot.jpa.domain.auth.token.config;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Setter
@Getter
@Component
@ConfigurationProperties("jwt") // properties파일에서 설정한 jwt를 가져오기 위함
public class JwtConfig {
 
    private String issuer;
 
    private String secretKeyString;

    private transient SecretKey javaxSecretKey; // 필드명을 secretKey로 설정하면 배포 단계에서 오류 발생함

    // 문자열 방식 대신 JWT 라이브러리의 최신 메서드 체인을 활용하여 HMAC SHA-256 알고리즘에 맞는 비밀 키를 생성하는 방식.
    // 생성된 비밀키를 통해 JWT 의 서명을 생성 및 복호화를 한다.
    // 빈 초기화 시 한번만 실행한다. 각각의 클래스에서 개별적으로 Jwts.SIG.HS256.key().build()로 생성할 시
    // secretKey가 새롭게 생성되어
    // JWT 서명 암호화 및 복호화에 서로 다른 secretKey를 사용할 수 있기 때문
    // 서버가 재 실행 될때마다 seceretkey가 다시 생성되어 refresh token 검증에 오류가 생김. 이에 따라 고정된 seceret
    // key를 사용.
    // 최종적으로 문자열 -> SecretKey 객체로 변환하는 작업이 필요하다.
    @PostConstruct
    public void init() {
        // 빈 초기화 시 비밀 키 생성
        this.javaxSecretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey getJwtSecretKey() {
        return this.javaxSecretKey; // 생성된 비밀 키 반환
    }
}
