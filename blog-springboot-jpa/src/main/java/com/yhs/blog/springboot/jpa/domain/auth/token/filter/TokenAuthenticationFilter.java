package com.yhs.blog.springboot.jpa.domain.auth.token.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.domain.auth.token.claims.ClaimsExtractor;
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
        private final ClaimsExtractor claimsExtractor;

        private final static String HEADER_AUTHORIZATION = "Authorization";
        private final static String TOKEN_PREFIX = "Bearer ";

        // 이 필터는 모든 HTTP 요청을 가로채고, 요청이 컨트롤러에 도달하기 전에 특정 로직을 처리
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                String method = request.getMethod();
                String requestURI = request.getRequestURI(); // requestURI는 이미 도메인이 제외된 상태, 예: /api/{blogId]/posts

                String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

                if (isPermitPostsRead(method, requestURI)) {

                        // 모든 사용자에게 허용하는 게시글들에 대한 요청일 때 게시글의 주인인지 판단하기 위함 -> 주인 이라면 해당 블로그 주인의 비공개 게시글
                        // 까지 조회하도록 함
                        log.info("[TokenAuthenticationFilter] isPermitPostsRead 메서드 결과가 참일때 진행");

                        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

                                log.info("[TokenAuthenticationFilter] isPermitPostsRead 메서드 - 액세스 토큰이 없는 비로그인 사용자 - 공개 글만 허용");

                                filterChain.doFilter(request, response);
                                return;
                        } else {

                                String accessToken = authorizationHeader.substring(TOKEN_PREFIX.length());

                                String requestedBlogId = extractBlogIdFromUri(requestURI);

                                if (!tokenValidator.validateAccessToken(accessToken)) {

                                        String blogIdFromExpiredToken = claimsExtractor
                                                        .extractBlogIdFromExpiredToken(accessToken);

                                        if (blogIdFromExpiredToken != null
                                                        && requestedBlogId.equals(blogIdFromExpiredToken)) {

                                                // 비공개 글을 보려면 보안상 토큰 재발급 받아야함
                                                log.info("[TokenAuthenticationFilter] isPermitPostsRead 메서드 - 블로그 주인의 만료된 액세스 토큰 - 액세스 토큰 재발급 필요");
                                                handleAuthenticationException(response,
                                                                "유효하지 않거나 만료된 토큰입니다.",
                                                                HttpServletResponse.SC_UNAUTHORIZED);

                                                return; // 유효하지 않은 토큰이면 필터 체인 종료, 다음 필터로 넘어가지 않음
                                        } else {
                                                log.info("[TokenAuthenticationFilter] isPermitPostsRead 메서드 - 일반 로그인 사용자의 만료된 토큰 - 공개 글만 허용");
                                                filterChain.doFilter(request, response);
                                                return;
                                        }

                                }

                                log.info("[TokenAuthenticationFilter] isPermitPostsRead 메서드 - 액세스 토큰이 유효한 사용자 분기 진행");

                                String blogIdFromToken = claimsExtractor.getBlogId(accessToken); // 만료되지 않은 토큰을 통해
                                                                                                 // blogId 추출

                                if (blogIdFromToken != null
                                                && requestedBlogId.equals(blogIdFromToken)) {

                                        log.info("[TokenAuthenticationFilter] isPermitPostsRead 메서드 - 액세스 토큰이 유효한 블로그 주인 분기 진행");

                                        Authentication authentication = authenticationProvider
                                                        .getAuthentication(accessToken);

                                        SecurityContextHolder.getContext().setAuthentication(authentication);
                                        filterChain.doFilter(request, response);
                                        return;
                                }

                                log.info("[TokenAuthenticationFilter] isPermitPostsRead 메서드 - 액세스 토큰이 유효한 일반 사용자 - 공개 글만 허용");
                                filterChain.doFilter(request, response);
                                return;

                        }

                }

                if (isPermitGetRequestExceptPermitPostsRead(method, requestURI)
                                || isPermitAllPostRequest(method, requestURI)) {
                        log.info("[TokenAuthenticationFilter] 토큰 검증 수행하지 않는 분기 진행, method:{} requestURI:{}", method,
                                        requestURI);
                        filterChain.doFilter(request, response);
                        return;
                }

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

        // URI에서 blogId 추출하는 메서드
        private String extractBlogIdFromUri(String requestURI) {
                // /api/{blogId}/posts 형식의 URI에서 blogId 추출, parts[0]은 "" 빈 문자열, 첫 번째 /
                String[] parts = requestURI.split("/");
                if (parts.length >= 3 && parts[1].equals("api")) {
                        return parts[2];
                }
                return null;
        }

        private boolean isPermitPostsRead(String method, String requestURI) {

                log.info("[TokenAuthenticationFilter] isPermitPostsRead 메서드 진행");

                if (!method.equals("GET")) {
                        return false;
                }

                return requestURI.matches("/api/[^/]+/posts") ||
                                requestURI.matches("/api/[^/]+/posts/page/[^/]+") ||
                                requestURI.matches("/api/[^/]+/categories") ||
                                requestURI.matches("/api/[^/]+/categories/[^/]+/posts") ||
                                requestURI.matches("/api/[^/]+/categories/[^/]+/posts/page/[^/]+") ||
                                requestURI.matches("/api/[^/]+/posts/[^/]+"); // 상세 페이지도 추가 작업 필요

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

        private boolean isPermitGetRequestExceptPermitPostsRead(String method, String requestURI) {

                log.info("[TokenAuthenticationFilter] isPermitGetRequestExceptPermitPostsRead 메서드 진행");

                if (!method.equals("GET")) {
                        return false;
                }

                return requestURI.equals("/api/token/initial-token") ||
                                requestURI.equals("/api/token/new-token") ||
                                requestURI.matches("/api/users/[^/]+/profile") ||
                                requestURI.startsWith("/api/check/") ||
                                requestURI.startsWith("/api/posts") ||
                                isSwaggerRequest(requestURI) ||
                                isActuatorRequest(requestURI);
        }

        private boolean isPermitAllPostRequest(String method, String requestURI) {

                log.info("[TokenAuthenticationFilter] isPermitAllPostRequest 메서드 진행");

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
