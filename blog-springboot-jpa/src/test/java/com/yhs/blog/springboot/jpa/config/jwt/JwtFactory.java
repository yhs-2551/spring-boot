package com.yhs.blog.springboot.jpa.config.jwt;

import io.jsonwebtoken.Jwts;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

@Getter
public class JwtFactory {

    @Autowired
    private JwtProperties jwtProperties;

    private String subject = "test@email.com";
    private Date issuedAt = new Date();
    private Date expiration = new Date(new Date().getTime() + Duration.ofDays(14).toMillis());
    private Map<String, Object> claims = emptyMap();

    @Builder
    public JwtFactory(String subject, Date issuedAt, Date expiration, Map<String, Object> claims) {
        this.subject = subject != null ? subject : this.subject;
        this.issuedAt = issuedAt != null ? issuedAt : this.issuedAt;
        this.expiration = expiration != null ? expiration : this.expiration;
        this.claims = claims != null ? claims : this.claims;
    }

    public static JwtFactory withDefaultValues() {
        return JwtFactory.builder().build();
    }

    public String createToken(JwtProperties jwtProperties) {
        return Jwts.builder().header().add("typ", "JWT").add("alg", "HS256").and().subject(subject).issuer(jwtProperties.getIssuer())
                .issuedAt(issuedAt).expiration(expiration).claims(claims).signWith(jwtProperties.getJwtSecretKey()
                        , Jwts.SIG.HS256).compact();
    }
}