package com.yhs.blog.springboot.jpa.domain.auth.token.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.AuthenticationProvider;
import com.yhs.blog.springboot.jpa.domain.auth.token.validation.TokenValidator;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Log4j2
public class TokenAuthenticationFilter extends OncePerRequestFilter {

        private final TokenValidator tokenValidator;
        private final AuthenticationProvider authenticationProvider;

        private final static String HEADER_AUTHORIZATION = "Authorization";
        private final static String TOKEN_PREFIX = "Bearer ";

        // 이 필터는 모든 HTTP 요청을 가로채고, 요청이 컨트롤러에 도달하기 전에 특정 로직을 처리
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                log.info("[TokenAuthenticationFilter] doFilterInternal d342424ggggggggggggggggggggggg.");

                String method = request.getMethod();
                String requestURI = request.getRequestURI();

                if (isPermitAllGetRequest(method, requestURI) || isPermitAllPostRequest(method, requestURI)) {
                        log.info("[TokenAuthenticationFilter] 토큰 검증 수행하지 않는 분기 진행");
                        filterChain.doFilter(request, response);
                        return;
                }

                String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

                if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

                        log.info("[TokenAuthenticationFilter] authorizationHeader Bearer 누락 분기 진행");

                        handleAuthenticationException(response,
                                        "액세스 토큰이 누락되었습니다.",
                                        HttpServletResponse.SC_UNAUTHORIZED);

                        return; // 요청 헤더에서 액세스 토큰을 찾을 수 없으면 필터 체인 종료, 다음 필터로 넘어가지 않음
                }

                String accessToken = authorizationHeader.substring(TOKEN_PREFIX.length());

                if (!tokenValidator.validateAccessToken(accessToken)) {

                        log.info("[TokenAuthenticationFilter] validateAccessToken 토큰 검증 실패 분기 진행");

                        handleAuthenticationException(response,
                                        "유효하지 않거나 만료된 토큰입니다.",
                                        HttpServletResponse.SC_UNAUTHORIZED);

                        return; // 유효하지 않은 토큰이면 필터 체인 종료, 다음 필터로 넘어가지 않음

                }

                log.info("[TokenAuthenticationFilter] validateAccessToken 통과 분기 진행");

                Authentication authentication = authenticationProvider.getAuthentication(accessToken);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
        }

        private boolean isSwaggerRequest(String requestURI) {
                return requestURI.equals("/swagger-ui.html") ||
                                requestURI.startsWith("/swagger-ui/") ||
                                requestURI.startsWith("/v3/api-docs") ||
                                requestURI.startsWith("/swagger-resources/");
        }

        private boolean isActuatorRequest(String requestURI) {
                return requestURI.startsWith("/actuator");
        }

        private boolean isPermitAllGetRequest(String method, String requestURI) {
                if (!method.equals("GET")) {
                        return false;
                }

                return requestURI.equals("/api/token/initial-token") ||
                                requestURI.equals("/api/token/new-token") ||
                                requestURI.matches("/api/[^/]+/posts") ||
                                requestURI.matches("/api/[^/]+/posts/page/[^/]+") ||
                                requestURI.matches("/api/[^/]+/posts/[^/]+") ||
                                requestURI.matches("/api/[^/]+/posts/[^/]+/edit") ||
                                requestURI.matches("/api/[^/]+/categories") ||
                                requestURI.matches("/api/[^/]+/categories/[^/]+/posts") ||
                                requestURI.matches("/api/[^/]+/categories/[^/]+/posts/page/[^/]+") ||
                                requestURI.matches("/api/users/[^/]+/profile") ||
                                requestURI.startsWith("/api/posts") ||
                                requestURI.startsWith("/api/check/") ||
                                isSwaggerRequest(requestURI) ||
                                isActuatorRequest(requestURI);
        }

        private boolean isPermitAllPostRequest(String method, String requestURI) {
                if (!method.equals("POST")) {
                        return false;
                }

                return requestURI.equals("/api/users/signup") ||
                                requestURI.equals("/api/auth/login") ||
                                requestURI.equals("/api/auth/logout") ||
                                requestURI.equals("/api/users/verify-code") ||
                                requestURI.equals("/api/oauth2/users") ||
                                requestURI.equals("/api/admin/batch/cleanup");
        }

        private void handleAuthenticationException(HttpServletResponse response,
                        String message, int status) throws IOException {

                ErrorResponse errorResponse = new ErrorResponse(message, status);
                response.setContentType("application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");

                response.setStatus(status);
                response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        }

}
