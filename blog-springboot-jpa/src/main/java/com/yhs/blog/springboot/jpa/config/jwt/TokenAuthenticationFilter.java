package com.yhs.blog.springboot.jpa.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

//    private String getAccessToken(String authorizationHeader) {
//
//        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
//            return authorizationHeader.substring(TOKEN_PREFIX.length());
//        }
//        return null;
//    }

    // 이 필터는 모든 HTTP 요청을 가로채고, 요청이 컨트롤러에 도달하기 전에 특정 로직을 처리
    // 얘를 어떻게 이용할지 고민, 그리고 초기에 왜 실패하는지 인증에.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // GET 요청에 대한 예외 처리
        if (method.equals("GET") && (requestURI.equals("/api/token/initial-token") ||  // 초기 토큰을
                // 가져오는 GET 요청이 필요 없음
                requestURI.startsWith("/api/posts")// 특정 게시글 조회 (GET /api/posts/{id}) 및 게시글 목록
                // 조회는 토큰 검증이 필요 없음
        )) {
            // 이 경로에 대해서는 필터를 적용하지 않고 다음 필터로 넘김
            filterChain.doFilter(request, response);
            return;
        }

        // POST 요청에 대한 예외 처리
        if (method.equals("POST") && (requestURI.startsWith("/api/user/") || requestURI.equals("/api/token/new-token"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

            System.out.println("authorizationHeader오류 실행 내부");

            // 상태 코드 401 설정
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // 응답 메시지 작성
            response.getWriter().write("Access token is missing");
            return; // 요청 헤더에서 액세스 토큰을 찾을 수 없으면 필터 체인 종료, 다음 필터로 넘어가지 않음
        }

        String accessToken = authorizationHeader.substring(TOKEN_PREFIX.length());

        System.out.println("실행 dofilterinternal 유효성검사 전");

        if (!tokenProvider.validToken(accessToken)) {

            System.out.println("실행 dofilterinternal 유효성검사 후 - 실패 ");
            // 상태 코드 401 설정
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // 응답 메시지 작성
            response.getWriter().write("Invalid or expired access token");
            return; // 유효하지 않은 토큰이면 필터 체인 종료, 다음 필터로 넘어가지 않음

        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        System.out.println("실행 dofilterinternal 유효성검사 후 - 성공 ");

        filterChain.doFilter(request, response);
    }

}


