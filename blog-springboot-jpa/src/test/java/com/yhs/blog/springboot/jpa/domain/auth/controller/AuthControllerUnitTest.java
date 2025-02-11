package com.yhs.blog.springboot.jpa.domain.auth.controller;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow; 
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc; 

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import com.yhs.blog.springboot.jpa.domain.auth.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.domain.auth.dto.response.LoginResultToken;
import com.yhs.blog.springboot.jpa.domain.auth.service.LoginProcessService;
import com.yhs.blog.springboot.jpa.domain.auth.service.LogoutProcessService;
import com.yhs.blog.springboot.jpa.web.cookie.TokenCookieManager;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.ExpiredJwtException;

import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 모든 필터 무시
@ActiveProfiles("test") 
public class AuthControllerUnitTest {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoSpyBean
        private AuthController authController; // AuthController 일부만 스터빙 하면서 AuthController의 기능 그대로 사용

        @MockitoBean
        private LogoutProcessService logoutProcessService;

        @MockitoBean
        private LoginProcessService loginProcessService;

        @MockitoBean
        private TokenCookieManager tokenCookieManager;

        @MockitoBean
        private HttpServletRequest httpServletRequest;

        @MockitoBean
        private JpaMetamodelMappingContext jpaMetamodelMappingContext;

        @Test
        @DisplayName("로그인에 성공했을 때 리프레시/액세스 토큰 발급 성공 테스트")
        void 로그인에_성공했을때_토큰_발급() throws Exception {
                // given
                LoginRequest loginRequest = new LoginRequest("test@example.com",
                                "Password123*", true);

                String refreshToken = "testRefreshToken";
                String accessToken = "testAccessToken";

                when(loginProcessService.loginUser(any(LoginRequest.class)))
                                .thenReturn(new LoginResultToken(refreshToken, accessToken));
                doNothing().when(authController).callSuperClearAuthenticationAttributes(any(HttpServletRequest.class));
                doNothing().when(tokenCookieManager).addRefreshTokenToCookie(any(HttpServletRequest.class),
                                any(HttpServletResponse.class), any(String.class), any(Boolean.class));

                // when & then
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Authorization", "Bearer " + accessToken))
                                .andDo(print());
                verify(loginProcessService).loginUser(any(LoginRequest.class));
                // 리프레시 토큰은 mockBean으로 해당 함수가 호출되었는지 동작만 검증 가능. MockBean이기 때문에 메서드 내부 로직이 실행
                // 안됨
                verify(tokenCookieManager).addRefreshTokenToCookie(any(), any(), any(), anyBoolean()); // any()는 객체 타입에
                                                                                                       // 대한 매핑,
                                                                                                       // primitive
                                                                                                       // type에는
                                                                                                       // anyBoolean()과
                                                                                                       // 같이 사용
                verify(authController).callSuperClearAuthenticationAttributes(any());
        }

        @Test
        @DisplayName("만료된 토큰으로 로그아웃 요청시 성공")
        void 만료된_토큰으로_로그아웃_요청시_성공() throws Exception {
                // given
                String expiredToken = "expiredToken";
                String authorizationHeader = "Bearer " + expiredToken;

                // void 메서드는 doThrow 사용
                doThrow(new ExpiredJwtException(null, null, "만료된 토큰"))
                                .when(logoutProcessService)
                                .logoutUser(expiredToken);

                try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
                        // when & then
                        mockMvc.perform(post("/api/auth/logout")
                                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("로그아웃에 성공하였습니다."))
                                        .andReturn();

                        // 쿠키 삭제 검증
                        cookieUtilMock.verify(() -> CookieUtil.deleteCookie(any(HttpServletRequest.class),
                                        any(HttpServletResponse.class),
                                        eq("refresh_token")));

                        cookieUtilMock.verify(() -> CookieUtil.deleteCookie(any(HttpServletRequest.class),
                                        any(HttpServletResponse.class),
                                        eq("access_token")));

                        verify(logoutProcessService).logoutUser(any());
                }
        }

        @Test
        @DisplayName("유효한 토큰으로 로그아웃 요청 시 로그아웃 성공")
        void 유효한_토큰으로_로그아웃_요청() throws Exception {
                // given
                String token = "validToken";
                String authorizationHeader = "Bearer " + token;

                try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {

                        doNothing().when(logoutProcessService).logoutUser(token);

                        // when & then
                        mockMvc.perform(post("/api/auth/logout")
                                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("로그아웃에 성공하였습니다."))
                                        .andReturn();

                        // 정적 메소드 검증 (Mockito-inline 방식)
                        cookieUtilMock.verify(() -> CookieUtil.deleteCookie(any(HttpServletRequest.class),
                                        any(HttpServletResponse.class),
                                        eq("refresh_token")));

                        cookieUtilMock.verify(() -> CookieUtil.deleteCookie(any(HttpServletRequest.class),
                                        any(HttpServletResponse.class),
                                        eq("access_token")));

                        verify(logoutProcessService).logoutUser(any());
                }
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 로그아웃 요청시 실패")
        void 변조된_토큰으로_로그아웃_요청시_실패() throws Exception {
                // given
                String invalidToken = "invalidToken";
                String authorizationHeader = "Bearer " + invalidToken;

                doThrow(new SignatureException("변조된 토큰입니다."))
                                .when(logoutProcessService)
                                .logoutUser(invalidToken);

                // when & then
                mockMvc.perform(post("/api/auth/logout")
                                .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("토큰이 유효하지 않습니다. 재 로그인 해주세요."))
                                .andReturn();

                // refreshToken은 삭제되지 않아야 함
                verify(logoutProcessService).logoutUser(any());

        }

}
