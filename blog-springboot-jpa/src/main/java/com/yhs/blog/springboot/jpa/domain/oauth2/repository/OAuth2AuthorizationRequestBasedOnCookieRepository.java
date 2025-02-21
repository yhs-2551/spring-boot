package com.yhs.blog.springboot.jpa.domain.oauth2.repository;

import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import com.yhs.blog.springboot.jpa.common.util.serialization.SerializationUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.util.WebUtils;

// 사용자 인증 정보 과정을 처리하는 클래스
@Log4j2
public class OAuth2AuthorizationRequestBasedOnCookieRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    // private final static int COOKIE_EXPIRE_SECONDS = 18000; // 300분 = 5시간
    // OAuth2 인증 요청 관련 쿠키는 인증 프로세스가 완료될 때까지만 유지되면 되므로, 짧게 설정. 5분으로 설정.
    private final static int COOKIE_EXPIRE_SECONDS = 300;

    public void removeAuthorizationRequestCookies(HttpServletRequest request,
            HttpServletResponse response) {
        // CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {

        log.info("[OAuth2AuthorizationRequestBasedOnCookieRepository] loadAuthorizationRequest() 메서드 시작");

        // 현재 사용자가 인증된 상태인지 확인, OAUTH2 로그인 성공 후 브라우저에서 페이지 새로고침하면 다시 실행되기 때문에 이 로직 추가
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {

            log.info(
                    "[OAuth2AuthorizationRequestBasedOnCookieRepository] loadAuthorizationRequest() 메서드 인증된 상태 분기 진행");

            return null; // 이미 인증된 경우 OAuth2AuthorizationRequest를 처리하지 않음
        }

        log.info(
                "[OAuth2AuthorizationRequestBasedOnCookieRepository] loadAuthorizationRequest() 메서드 인증되지 않은 상태 분기 진행");

        // 쿠키에서 해당 이름의 쿠키를 가져옴
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);

        // 쿠키를 가져오지 못했다면 초기에 구글 로그인 페이지로 리다이렉트 하기 전의 쿠키 정보가 아니라는 의미.
        if (cookie == null) {

            log.info(
                    "[OAuth2AuthorizationRequestBasedOnCookieRepository] loadAuthorizationRequest() 메서드 쿠키가 null 분기 진행");
            return null;
        }

        log.info(
                "[OAuth2AuthorizationRequestBasedOnCookieRepository] loadAuthorizationRequest() 메서드 쿠키가 null이 아닌 경우 분기 진행");

        return SerializationUtils.deserialize(cookie, OAuth2AuthorizationRequest.class); // String 값을 역직렬화

    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
            HttpServletResponse response) {

        log.info("[OAuth2AuthorizationRequestBasedOnCookieRepository] saveAuthorizationRequest() 메서드 시작");

        if (authorizationRequest == null) {

            log.info(
                    "[OAuth2AuthorizationRequestBasedOnCookieRepository] saveAuthorizationRequest() 메서드 authorizationRequest == null 분기 진행");
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
        SerializationUtils.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
            HttpServletResponse response) {

        log.info("[OAuth2AuthorizationRequestBasedOnCookieRepository] removeAuthorizationRequest() 메서드 시작");

        return this.loadAuthorizationRequest(request);
    }
}
