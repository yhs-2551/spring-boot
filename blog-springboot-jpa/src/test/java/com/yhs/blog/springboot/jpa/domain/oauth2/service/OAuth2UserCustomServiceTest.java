package com.yhs.blog.springboot.jpa.domain.oauth2.service;
 
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension; 
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2UserCustomServiceTest {

    @InjectMocks
    private OAuth2UserLoadService oAuth2UserLoadService;

    @Mock
    OAuth2UserRequest oAuth2UserRequest;

    @Mock
    OAuth2User oAuth2User;

    @Test
    @DisplayName("이메일이 없는 경우 예외 발생")
    void 사용자를_불러온후_이메일이_없는_경우_예외_발생() {
        // given
        OAuth2UserLoadService serviceSpy = spy(oAuth2UserLoadService); // 실제 외부 api 호출할 수 없도록 spy로 변경. 일부만 스터빙하고 싶을 때 사용. 나머지는 스터빙x
        when(oAuth2User.getAttribute("email")).thenReturn(null);
        doReturn(oAuth2User).when(serviceSpy).callSuperLoadUser(oAuth2UserRequest);

        // when & then
        assertThrows(SystemException.class, () -> serviceSpy.loadUser(oAuth2UserRequest));
    }

    @Test
    @DisplayName("이메일이 있는 경우 성공")
    void loadUser_WithEmail_Success() {
        // given
        OAuth2UserLoadService serviceSpy = spy(oAuth2UserLoadService);
        when(oAuth2User.getAttribute("email")).thenReturn("test@email.com");
        doReturn(oAuth2User).when(serviceSpy).callSuperLoadUser(oAuth2UserRequest);

        // when
        OAuth2User result = serviceSpy.loadUser(oAuth2UserRequest);
        // then
        assertThat(result).isEqualTo(oAuth2User);
    }
}