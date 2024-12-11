// OAuth2 에서 제공해주는 사용자 정보를 가져와서 최종적으로 해당 OAuth2 사용자를 리턴. 이 사용자를 OAuth2SuccessHandler 에서 사용

package com.yhs.blog.springboot.jpa.domain.oauth2.service;

import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class OAuth2UserCustomService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // DB에 OAuth2 User를 업데이트 및 새로 저장하는 메서드
//    @Transactional
//    private void saveOAuth2User(OAuth2User oAuth2User) {
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        String email = (String) attributes.get("email");
//        String name = (String) attributes.get("name");
//
//        try {
//            User user = userRepository.findByEmail(email)
//                    .orElse(User.builder().blogId(UUID.randomUUID().toString()).email(email).username(name).build());
//            // 블로그 ID는 NULL 값이 되면 안되기 때문에 임시 블로그 ID 발급
//            userRepository.save(user);
//        } catch (Exception e) {
//            log.error("Error creating user with email: " + email, e);
//            throw new UserCreationException("이미 존재하는 사용자 입니다.");
//        }
//
//    }


    // 최종적으로 OAuth2User를 리턴. 내 블로그 시스템에선 OAuth2 username을 사용하지 않고, 직접 사용자가 지정한 블로그 닉네임을 사용할것이기 때문에 기존의 사용자여도 따로
    // username을 업데이트 하지 않음. 만약 OAuth2 사용자명을 사용했다면, 기존의 사용자여도 사용자명을 업데이트 해야함.
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        return oAuth2User;
    }

}
