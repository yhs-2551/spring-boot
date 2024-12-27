package com.yhs.blog.springboot.jpa.domain.oauth2.handler;

import com.yhs.blog.springboot.jpa.domain.oauth2.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenManagementService;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Log4j2
// OAUTH2의 경우 로그인 유지 기간을 어떻게 할지 고민 임시로 리멤버미와 똑같은 기간으로 구현 했음
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String REDIRECT_PATH = "http://localhost:3000/";

    private final TokenProvider tokenProvider;
    private final TokenManagementService tokenManagementService;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository auth2AuthorizationRequestBasedOnCookieRepository;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;
    private static final long REGISTRATION_TIMEOUT_HOURS_TTL = 1;

    // 토큰과 관련된 작업만 추가로 처리하기 위한 메서드
    // 나중에 리멤버미 Redis 기반 처리 추가 필요
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // OAuth2UserCustomService에서 리턴한 oAuth2User는 세션 스코프로 저장되어 있는데, 해당 OAuth2 사용자를 가져옴
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Optional<User> user = userService.findUserByEmail((String) oAuth2User.getAttributes().get("email"));

        Cookie uniqueIdCookie = WebUtils.getCookie(request, "oauth2_remember_me_unique_id");
        assert uniqueIdCookie != null;
        String uniqueId = uniqueIdCookie.getValue();

        String rememberMe = redisTemplate.opsForValue().get("TEMP_RM:" + uniqueId);
        redisTemplate.delete("TEMP_RM:" + uniqueId);

        boolean isRememberMe = Boolean.parseBoolean(rememberMe);

        String targetUrl;

        if (user.isEmpty()) { // OAuth2 신규사용자의 경우

            String newOAuth2UserEmail = (String) oAuth2User.getAttributes().get("email");
            String tempOAuth2UserUniqueId = UUID.randomUUID().toString(); // 추가 정보를 입력하고 POST 요청했을 때 특정 사용자를 식별하기 위함.
            targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_PATH + "/oauth2/redirect").queryParam("showNewUserModal", true).queryParam("tempOAuth2UserUniqueId", tempOAuth2UserUniqueId).build().toUriString();
            // OAuth2 신규 등록 시간을 넉넉하게 1시간으로 설정
            redisTemplate.opsForValue().set("RM:" + newOAuth2UserEmail, String.valueOf(isRememberMe),
                    REGISTRATION_TIMEOUT_HOURS_TTL, TimeUnit.HOURS);
            // 추가 정보를 입력하고 최종적으로 요청했을때 특정 사용자를 식별해서 이메일 값을 가져와야하기 때문에 아래 로직 추가
            redisTemplate.opsForValue().set("TEMP_OAUTH2_USER_EMAIL:" + tempOAuth2UserUniqueId, newOAuth2UserEmail,
                    REGISTRATION_TIMEOUT_HOURS_TTL, TimeUnit.HOURS);

        } else {
            // OAuth2 기존 사용자의 경우
            targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_PATH + "/oauth2/redirect").queryParam("direct", true).build().toUriString();

            // 리프레시 토큰 생성
            String refreshToken;
            if (isRememberMe) {
                refreshToken = tokenProvider.generateToken(user.get(),
                        TokenManagementService.REMEMBER_ME_REFRESH_TOKEN_DURATION);
                redisTemplate.opsForValue().set(TokenManagementService.RT_PREFIX + user.get().getEmail(), refreshToken,
                        TokenManagementService.REMEMBER_ME_REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

            } else {
                refreshToken = tokenProvider.generateToken(user.get(), TokenManagementService.REFRESH_TOKEN_DURATION);
                redisTemplate.opsForValue().set(TokenManagementService.RT_PREFIX + user.get().getEmail(), refreshToken,
                        TokenManagementService.REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

            }

            // 생성된 리프레시 토큰을 클라이언트측 쿠키에 저장 -> 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하기 위함
            tokenManagementService.addRefreshTokenToCookie(request, response, refreshToken, isRememberMe);

            // Access Token 생성
            String accessToken = tokenProvider.generateToken(user.get(), TokenManagementService.ACCESS_TOKEN_DURATION);

            // 초기에 액세스 토큰을 쿠키에 발급 리다이렉트하면 바로 프론트측에서 응답헤더에 접근할 수 없기 때문에.
            tokenManagementService.handleAccessTokenCookie(request, response, accessToken);

        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }

    //    부모 클래스에 다른 시그니처(매개변수와 리턴 타입)를 가진 메서드이기 때문에 override가 아닌 overloading이 된다.
    //  OAuth2 인증을 위해 임시로 세션에 저장된 정보나 쿠키의 정보를 제거하여 남아있는 데이터를 정리.
    //  세션이나 쿠키에 불필요한 데이터가 남아 있지 않도록 하여 보안을 강화함.
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {

        //인증 실패와 관련된 정보를 세션에서 제거. 즉 다음에 재로그인할때 만약 이전 인증 실패 정보가 남아있다면 이전 인증 실패 정보가 남아있지 않도록 함.
        super.clearAuthenticationAttributes(request);

        // OAuth2 인증 과정에서 저장된 쿠키를 삭제하여 클라이언트 측의 인증 관련 데이터를 정리.
        auth2AuthorizationRequestBasedOnCookieRepository.removeAuthorizationRequestCookies(request, response);
    }

}

