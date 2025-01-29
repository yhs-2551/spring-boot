package com.yhs.blog.springboot.jpa.domain.auth.token.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.common.constant.code.StatusCode;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.AuthenticationProvider;
import com.yhs.blog.springboot.jpa.domain.auth.token.validation.TokenValidator;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
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

                log.info("[TokenAuthenticationFilter] doFilterInternal 메서드 시작");

                String requestURI = request.getRequestURI();
                String method = request.getMethod();

                // GET 요청에 대한 예외 처리
                // 초기 토큰을 가져오는 GET 요청의 경우 검증 필요 없음
                if (method.equals("GET") &&
                                (requestURI.equals("/api/token/initial-token") ||
                                                requestURI.equals("/api/token/new-token") ||
                                                // 특정 게시글 조회 (GET /api/posts/{id}) 및 게시글 목록 조회는 토큰 검증이 필요 없음
                                                requestURI.matches("/api/[^/]+/posts") ||
                                                requestURI.matches("/api/[^/]+/posts/page/[^/]+") ||
                                                requestURI.matches("/api/[^/]+/posts/[^/]+") ||
                                                requestURI.matches("/api/[^/]+/categories") ||
                                                requestURI.matches("/api/[^/]+/categories/[^/]+/posts") ||
                                                requestURI.matches("/api/[^/]+/categories/[^/]+/posts/page/[^/]+") ||
                                                requestURI.matches("/api/users/[^/]+/profile") ||
                                                requestURI.startsWith("/api/posts") ||
                                                // username, Email, BlogId 체크는 토큰 검증이 필요 없음
                                                requestURI.startsWith("/api/check/") ||
                                                // Swagger UI 관련 요청은 토큰 검증이 필요 없음
                                                requestURI.equals("/swagger-ui.html") ||
                                                requestURI.startsWith("/swagger-ui/") ||
                                                requestURI.startsWith("/v3/api-docs") ||
                                                requestURI.startsWith("/swagger-resources/"))) {

                        log.info("[TokenAuthenticationFilter] GET 요청 토큰 검증 수행하지 않는 분기 진행");

                        // 이 경로에 대해서는 필터를 적용하지 않고 다음 필터로 넘김
                        filterChain.doFilter(request, response);
                        return;
                }

                // POST 요청에 대한 예외 처리
                // 로그아웃은 필터에서는 통과 시키고 컨트롤러에서 따로 처리.
                if (method.equals("POST") &&
                                (requestURI.equals("/api/users/signup") ||
                                                requestURI.equals("/api/auth/login") ||
                                                requestURI.equals("/api/auth/logout") ||
                                                requestURI.equals("/api/users/verify-code") ||
                                                requestURI.equals("/api/oauth2/users") ||
                                                requestURI.equals("/api/admin/batch/cleanup") // 배치 테스트용으로 추가
                                )) {

                        log.info("[TokenAuthenticationFilter] POST 요청 토큰 검증 수행하지 않는 분기 진행");
                        filterChain.doFilter(request, response);
                        return;
                }

                String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

                if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

                        log.info("[TokenAuthenticationFilter] authorizationHeader 오류 분기 진행");

                        ErrorResponse errorResponse = new ErrorResponse(
                                        "액세스 토큰이 누락되었습니다.",
                                        HttpStatus.UNAUTHORIZED.value());

                        // Content-Type 설정
                        response.setContentType("application/json;charset=UTF-8");
                        response.setCharacterEncoding("UTF-8");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                        // 응답 메시지 작성
                        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
                        return; // 요청 헤더에서 액세스 토큰을 찾을 수 없으면 필터 체인 종료, 다음 필터로 넘어가지 않음
                }

                String accessToken = authorizationHeader.substring(TOKEN_PREFIX.length());

                if (!tokenValidator.validateAccessToken(accessToken)) {

                        log.info("[TokenAuthenticationFilter] validateAccessToken 오류 분기 진행");

                        ErrorResponse errorResponse = new ErrorResponse(
                                        "유효하지 않거나 만료된 토큰입니다.",
                                        HttpStatus.UNAUTHORIZED.value());

                        log.debug("실행 dofilterinternal 유효성검사 후 - 실패 ");
                        // Content-Type 설정
                        response.setContentType("application/json;charset=UTF-8");
                        response.setCharacterEncoding("UTF-8");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        // 응답 메시지 작성
                        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
                        return; // 유효하지 않은 토큰이면 필터 체인 종료, 다음 필터로 넘어가지 않음

                }

                log.info("[TokenAuthenticationFilter] validateAccessToken 통과 분기 진행");

                Authentication authentication = authenticationProvider.getAuthentication(accessToken);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
        }

}
