package com.yhs.blog.springboot.jpa.security.oauth2.handler;

import com.yhs.blog.springboot.jpa.security.jwt.service.TokenManagementService;
import com.yhs.blog.springboot.jpa.security.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.security.jwt.service.TokenService;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.security.oauth2.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    public static final String REDIRECT_PATH = "http://localhost:3000/";

    private final TokenProvider tokenProvider;
    private final TokenManagementService tokenManagementService;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository auth2AuthorizationRequestBasedOnCookieRepository;
    private final UserService userService;
    private final TokenService tokenService;

    // 토큰과 관련된 작업만 추가로 처리하기 위한 메서드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = userService.findUserByEmail((String) oAuth2User.getAttributes().get("email"));

        // 리프레시 토큰 생성
        String refreshToken = tokenProvider.generateToken(user, TokenManagementService.REFRESH_TOKEN_DURATION);

        // 리프레시 토큰을 userId와 함께 DB에 저장
        tokenManagementService.saveRefreshToken(user.getId(), refreshToken);

        // 생성된 리프레시 토큰을 클라이언트측 쿠키에 저장 -> 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하기 위함
        tokenManagementService.addRefreshTokenToCookie(request, response, refreshToken);

        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenManagementService.ACCESS_TOKEN_DURATION);

        // 액세스 토큰을 리다이렉트 경로에 파라미터로 추가 근데 이 방식은 보안 상 별로라 사용하지 않는다.
//        String targetUrl = getTargetUrl(accessToken);
        tokenManagementService.handleAccessTokenCookie(request, response, accessToken);

        clearAuthenticationAttributes(request, response);

        getRedirectStrategy().sendRedirect(request, response, REDIRECT_PATH);
    }

    // 액세스 토큰을 리다이렉트 경로에 파라미터로 추가. 보안상의 이유로 일단 사용하지 않음. 헤더 방식 사용
//    private String getTargetUrl(String token) {
//        return UriComponentsBuilder.fromUriString(REDIRECT_PATH).queryParam("token", token).build().toUriString();
//
//    }


    //    부모 클래스에 다른 시그니처(매개변수와 리턴 타입)를 가진 메서드이기 때문에 override가 아닌 overloading이 된다.
//  OAuth2 인증을 위해 임시로 세션에 저장된 정보나 쿠키의 정보를 제거하여 남아있는 데이터를 정리.
//  세션이나 쿠키에 불필요한 데이터가 남아 있지 않도록 하여 보안을 강화함.
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse
            response) {

        //인증 후 세션에 남아있는 불필요한 정보 제거
        super.clearAuthenticationAttributes(request);

        // OAuth2 인증 과정에서 저장된 쿠키를 삭제하여 클라이언트 측의 인증 관련 데이터를 정리.
        auth2AuthorizationRequestBasedOnCookieRepository.removeAuthorizationRequestCookies(request, response);
    }

}

