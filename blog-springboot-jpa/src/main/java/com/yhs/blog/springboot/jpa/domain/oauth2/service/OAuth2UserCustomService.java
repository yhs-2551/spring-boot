// OAuth2 에서 제공해주는 사용자 정보를 가져와서 최종적으로 해당 OAuth2 사용자를 리턴. 이 사용자를 OAuth2SuccessHandler 에서 사용

package com.yhs.blog.springboot.jpa.domain.oauth2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    protected OAuth2User callSuperLoadUser(OAuth2UserRequest request) {
        return super.loadUser(request);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = callSuperLoadUser(request);
        if (oAuth2User.getAttribute("email") == null) {
            throw new OAuth2AuthenticationException("사용자를 불러오지 못했습니다.");
        }
        return oAuth2User;
    }
}
