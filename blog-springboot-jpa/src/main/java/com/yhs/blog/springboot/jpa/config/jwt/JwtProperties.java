package com.yhs.blog.springboot.jpa.config.jwt;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Setter
@Getter
@Component
@ConfigurationProperties("jwt") // properties파일에서 설정한 jwt를 가져오기 위함
public class JwtProperties {
    private String issuer;
    private SecretKey secretKey;

    // 문자열 방식 대신 JWT 라이브러리의 최신 메서드 체인을 활용하여 HMAC SHA-256 알고리즘에 맞는 비밀 키를 생성하는 방식.
    // 생성된 비밀키를 통해 JWT 의 서명을 생성 및 복호화를 한다. 
    // 빈 초기화 시 한번만 실행한다. 각각의 클래스에서 개별적으로  Jwts.SIG.HS256.key().build()로 생성할 시 secretKey가 새롭게 생성되어
    // JWT 서명 암호화 및 복호화에 서로 다른 secretKey를 사용할 수 있기 때문
    @PostConstruct
    public void init() {
        // 빈 초기화 시 비밀 키 생성
        this.secretKey = Jwts.SIG.HS256.key().build();
    }

    public SecretKey getJwtSecretKey() {
        return this.secretKey; // 생성된 비밀 키 반환
    }
}

