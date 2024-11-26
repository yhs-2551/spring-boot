package com.yhs.blog.springboot.jpa.domain.oauth2.service;

import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    // DB에 OAuth2 User를 업데이트 및 새로 저장하는 메서드
    private User saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        User user = userRepository.findByEmail(email)
                .map(userEntity -> userEntity.update(name))
                .orElse(User.builder().email(email).username(name).build());
        return userRepository.save(user);
    }

    // User 테이블에 사용자 정보가 있다면 이름을 업데이트하고, 없다면 saveOrUpdate()메서드를 실행해 users 테이블에 회원 데이터를 추가한다.
    // 최종적으로 OAuth2User를 리턴한다.
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(oAuth2UserRequest);
        saveOrUpdate(user);
        return user;
    }


}
