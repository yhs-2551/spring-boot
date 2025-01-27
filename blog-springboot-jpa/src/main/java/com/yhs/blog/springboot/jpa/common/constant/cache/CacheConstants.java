package com.yhs.blog.springboot.jpa.common.constant.cache;

public final class CacheConstants {
    private CacheConstants() {
    }

    public static final long DUPLICATE_CHECK_CACHE_HOURS = 6L; // 중복확인의 경우 메모리 낭비가 될 수 있기 때문에 6시간으로 설정

    public static final long PROFILE_CACHE_HOURS = 24L; // 프론트는 12시간, redis 캐시를 활용해 DB 부하를 감소하기 위해 2배인 24시간으로 설정

    // public static final long CACHE_TTL = 24 * 60 * 60; // 1일

}
