// OAuth2 에서 제공해주는 사용자 정보를 가져와서 최종적으로 해당 OAuth2 사용자를 리턴. 이 사용자를 OAuth2SuccessHandler 에서 사용

package com.yhs.blog.springboot.jpa.domain.oauth2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode; 
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

@RequiredArgsConstructor
@Service
@Log4j2
public class OAuth2UserLoadService extends DefaultOAuth2UserService {

    protected OAuth2User callSuperLoadUser(OAuth2UserRequest request) {
        return super.loadUser(request);
    }

    @Override
    @Loggable
    public OAuth2User loadUser(OAuth2UserRequest request) {

        log.info("[OAuth2UserLoadService] loadUser 메서드 시작");

        OAuth2User oAuth2User = callSuperLoadUser(request);
        if (oAuth2User.getAttribute("email") == null) {
            throw new SystemException(
                    ErrorCode.OAUTH2_USER_LOAD_FAIL,
                    "소셜 로그인 처리 중 오류가 발생했습니다. 다시 시도해주세요.",
                    "OAuth2UserLoadService",
                    "loadUser");
            // throw new OAuth2AuthenticationException("사용자를 불러오지 못했습니다.");
        }
        return oAuth2User;
    }
}
