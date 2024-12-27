package com.yhs.blog.springboot.jpa.domain.oauth2.filter;

import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// 리멤버미 선택하고, OAuth2 로그인 클릭 시 해당 Remeber-me 값을 유지하기 위한 필터
@Log4j2
@RequiredArgsConstructor
public class RememberMeAuthenticationFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/oauth2/authorization/")) {
            String rememberMe = request.getParameter("remember_me");

            if (rememberMe != null) {
                String uniqueId = UUID.randomUUID().toString();
                // 쿠키는 사용자의 브라우저마다 개별적으로 저장되지만, 더욱 명확한 사용자 구분을 위해 Redis 사용
                CookieUtil.addCookie(response, "oauth2_remember_me_unique_id", uniqueId, 180);
                redisTemplate.opsForValue().set("TEMP_RM:" + uniqueId, rememberMe, 180, TimeUnit.SECONDS);
            }

        }

        filterChain.doFilter(request, response);
    }
}
