package com.yhs.blog.springboot.jpa.config.oauth;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.entity.RefreshToken;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.repository.RefreshTokenRepository;
import com.yhs.blog.springboot.jpa.service.impl.UserServiceImpl;
import com.yhs.blog.springboot.jpa.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
//    public static final String REDIRECT_PATH = "/api/posts";

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository auth2AuthorizationRequestBasedOnCookieRepository;
    private final UserServiceImpl userService;

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId).map(entity -> entity.update(newRefreshToken)).orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);

    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    // 액세스 토큰을 리다이렉트 경로에 파라미터로 추가. 보안상의 이유로 일단 사용하지 않음. 헤더 방식 사용
//    private String getTargetUrl(String token) {
//        return UriComponentsBuilder.fromUriString(REDIRECT_PATH).queryParam("token", token).build().toUriString();
//
//    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {

        //인증 후 세션에 남아있는 불필요한 정보 제거
        super.clearAuthenticationAttributes(request);

        // OAuth2 인증 과정에서 저장된 쿠키를 삭제하여 클라이언트 측의 인증 관련 데이터를 정리.
        auth2AuthorizationRequestBasedOnCookieRepository.removeAuthorizationRequestCookies(request, response);
    }

    // 토큰과 관련된 작업만 추가로 처리하기 위한 메서드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = userService.findUserByEmail((String)oAuth2User.getAttributes().get("email"));

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

        // 응답 헤더에 액세스 토큰을 추가
        response.addHeader("Authorization", "Bearer " + accessToken);

//        String targetUrl = REDIRECT_PATH;


        // 인증 후 세션 속성 정리. 아마 JWT방식을 사용하기 때문에 세션 관련은 필요없음
        // OAuth2 관련 쿠키 삭제
        clearAuthenticationAttributes(request, response);

        // 리다이렉트. REST 방식에서는 굳이 필요 없음
//        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }


}
