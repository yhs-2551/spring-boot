package com.yhs.blog.springboot.jpa.common.constant.token;

import java.time.Duration;

public final class TokenConstants {

    // 인스턴스화 방지 
    private TokenConstants() {

    }

    // 쿠키 이름 관련 상수
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    // 토큰 만료 시간 관련 상수
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(1);
    public static final Duration REMEMBER_ME_REFRESH_TOKEN_DURATION = Duration.ofDays(14);

    // Redis TTL 관련 상수
    public static final long REFRESH_TOKEN_TTL = Duration.ofDays(1).toSeconds();
    public static final long REMEMBER_ME_REFRESH_TOKEN_TTL = Duration.ofDays(14).toSeconds();

    // Redis key prefix
    public static final String RT_PREFIX = "RT:";

}
