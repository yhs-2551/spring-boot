package com.yhs.blog.springboot.jpa.domain.oauth2.service;

import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2UserCustomServiceTest {

    @InjectMocks
    private OAuth2UserCustomService oAuth2UserCustomService;

    @Test
    @DisplayName("이메일이 없는 경우 예외 발생")
    void loadUser_NoEmail_ThrowsException() {
        // given
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        OAuth2UserCustomService serviceSpy = spy(oAuth2UserCustomService);
        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getAttribute("email")).thenReturn(null);
        doReturn(mockUser).when(serviceSpy).callSuperLoadUser(request);

        // when & then
        assertThrows(OAuth2AuthenticationException.class, () -> serviceSpy.loadUser(request));
    }

    @Test
    @DisplayName("이메일이 있는 경우 성공")
    void loadUser_WithEmail_Success() {
        // given
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);
        OAuth2UserCustomService serviceSpy = spy(oAuth2UserCustomService);
        OAuth2User mockUser = mock(OAuth2User.class);
        when(mockUser.getAttribute("email")).thenReturn("test@email.com");
        doReturn(mockUser).when(serviceSpy).callSuperLoadUser(request);

        // when
        OAuth2User result = serviceSpy.loadUser(request);

        // then
        assertThat(result).isEqualTo(mockUser);
    }
}