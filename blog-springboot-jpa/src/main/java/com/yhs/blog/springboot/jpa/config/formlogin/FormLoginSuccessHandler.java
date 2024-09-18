package com.yhs.blog.springboot.jpa.config.formlogin;


import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;

import com.yhs.blog.springboot.jpa.entity.RefreshToken;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.repository.RefreshTokenRepository;
import com.yhs.blog.springboot.jpa.service.impl.TokenServiceImpl;
import com.yhs.blog.springboot.jpa.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class FormLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenServiceImpl tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

//        SecurityContextHolder에 저장되어 있는 사용자 주체를 꺼내옴
        User user = (User) authentication.getPrincipal();

        String getRefreshTokenCookie = getRefreshTokenCookie(request);

        // Form로그인을 통하여 동일한 사용자가 2번이상 로그인 & 브라우저 쿠키에 리프레시 토큰이 있을 때.
        // 해당 RefreshToken을 이용해 새로운 액세스 토큰 발급
        if (getRefreshTokenCookie != null && tokenProvider.validToken(getRefreshTokenCookie)) {

            // 리프레시 토큰이 유효하다면 새로운 액세스 토큰 발급
            String newAccessToken = tokenService.createNewAccessToken(getRefreshTokenCookie);

            handleAccessTokenCookie(request, response, newAccessToken);

        } else {

            // else문은 FormLogin에 초기 로그인 시

            // 리프레시 토큰 생성
            String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);

            // 리프레시 토큰을 userId와 함께 DB에 저장
            saveRefreshToken(user.getId(), refreshToken);

            // 생성된 리프레시 토큰을 클라이언트측 쿠키에 저장 -> 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하기 위함
            addRefreshTokenToCookie(request, response, refreshToken);

            // Access Token 생성
            String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);

            // 액세스 토큰을 리다이렉트 경로에 파라미터로 추가 근데 이 방식은 보안 상 별로라 사용하지 않는다.
//        String targetUrl = getTargetUrl(accessToken);

            // 생성된 액세스 토큰을 클라이언트 측 쿠키에 저장. 응답헤더에 바로 담아서 주면 redirect로 페이지 변경 시 클라이언트측에서 응답 헤더에 바로 
            // 접근 불가능 하기 때문
            handleAccessTokenCookie(request, response, accessToken);
        }

    }


    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId).map(entity -> entity.update(newRefreshToken)).orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);

    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse
            response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    private String getRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void handleAccessTokenCookie(HttpServletRequest request, HttpServletResponse
            response, String accessToken) {
//        초기 로그인 시 HTTP-only 쿠키에 액세스 토큰 설정. 응답 헤더에 바로 담아서 주면 JavaScript 코드에서 이 헤더에 접근할 수 없다.
//        브라우저는 보안상의 이유로 리다이렉트 응답의 헤더를 자바스크립트에서 읽을 수 없게 하고 있기 때문이다.
//        브라우저에서 redirect가 발생할 때, 리디렉션 응답 자체의 헤더는 클라이언트에서 접근할 수 없다
//        즉, getRedirectStrategy() .sendRedirect() 메서드를 사용하면, 리디렉션된 페이지에서 응답 헤더를 클라이언트가 읽을 수 없다는
//        점을 염두에 두어야 한다.
//      액세스 토큰 HTTP Only 쿠키 저장은, 초기에 응답 헤더로 액세스 토큰을 전송해줄때만 사용하므로 setMaxAge를 60초만 설정. 지정하지 않을 수도 있음.
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true); // javascript 에서 접근 불가
        accessTokenCookie.setSecure(false); // true면 HTTPS에서만 전달, 배포 시에 true로 변경 필요
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60); // 1분 60초.

        response.addCookie(accessTokenCookie);

        //  세션이나 쿠키에 불필요한 데이터가 남아 있지 않도록 하여 보안을 강화함.
        super.clearAuthenticationAttributes(request);
    }
}

