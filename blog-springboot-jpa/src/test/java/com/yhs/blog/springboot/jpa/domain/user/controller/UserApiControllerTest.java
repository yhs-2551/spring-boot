//package com.yhs.blog.springboot.jpa.domain.user.controller;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
//import com.yhs.blog.springboot.jpa.domain.user.entity.User;
//import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
//import com.yhs.blog.springboot.jpa.domain.user.dto.request.LoginRequest;
//import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
//import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;
//import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
//import com.yhs.blog.springboot.jpa.domain.token.jwt.service.RefreshTokenService;
//import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenManagementService;
//import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenService;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.mock.web.MockHttpSession;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(UserApiController.class)
//@AutoConfigureMockMvc(addFilters = false) // Security 모든 필터 무시
//@MockBean(JpaMetamodelMappingContext.class) // JPA Auditing 관련 설정 무시
//class UserApiControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private UserService userService;
//
//    @MockBean
//    private AuthenticationManager authenticationManager;
//
//    @MockBean
//    private TokenProvider tokenProvider;
//
//    @MockBean
//    private RefreshTokenService refreshTokenService;
//
//    @MockBean
//    private TokenManagementService tokenManagementService;
//
//    @MockBean
//    private TokenService tokenService;
//
//    @MockBean
//    private CookieUtil cookieUtil;
//
//    @Nested
//    @DisplayName("회원가입 API 테스트")
//    class SignUp {
//        @Test
//        @DisplayName("정상적인 요청이면 회원가입이 성공")
//        void signup_success() throws Exception {
//            // given
//            SignUpUserRequest request = new SignUpUserRequest(
//                    "testBlogId",
//                    "testUser",
//                    "test@example.com",
//                    "password123"
//            );
//            SignUpUserResponse response = new SignUpUserResponse(
//                    1L,
//                    "testBlogId",
//                    "testUser",
//                    "test@example.com"
//            );
//            when(userService.createUser(any(SignUpUserRequest.class))).thenReturn(response);
//            // when & then
//            mockMvc.perform(post("/api/users/signup")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isCreated())
//                    .andExpect(jsonPath("$.data.id").value(1L))
//                    .andExpect(jsonPath("$.data.blogId").value("testBlogId"))
//                    .andExpect(jsonPath("$.data.userName").value("testUser"))
//                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
//                    .andExpect(jsonPath("$.message").value("User created successfully."));
//
//            verify(userService).createUser(any(SignUpUserRequest.class));
//        }
//    }
//
//
//    @Nested
//    @DisplayName("로그인 API 테스트")
//    class Login {
//        @Test
//        @DisplayName("정상적인 요청인 경우 로그인 동작 및 리프레시/액세스 토큰 발급 검증")
//        void login_success_with_accees_token_refresh_token() throws Exception {
//            // given
//            LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
//            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
//            Authentication authentication = Mockito.mock(Authentication.class);
//            when(authenticationManager.authenticate(authToken)).thenReturn(authentication);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            User user = Mockito.mock(User.class);
//            when(authentication.getPrincipal()).thenReturn(user);
//            when(user.getId()).thenReturn(1L);
//
//
////            String refreshToken = "refreshToken";
//            String accessToken = "accessToken";
//            when(tokenManagementService.getRefreshTokenCookie(any())).thenReturn(null);
////            when(tokenProvider.generateToken(user, TokenManagementService.REFRESH_TOKEN_DURATION)).thenReturn(refreshToken);
//            when(tokenProvider.generateToken(user, TokenManagementService.ACCESS_TOKEN_DURATION)).thenReturn(accessToken);
//
//
//            // Mock HttpServletRequest와 HttpSession 설정.
//            // super.clearAuthenticationAttributes (request); 내부 동작 검증을 위함. protected 메서드이므로 직접 호출 불가.
//            // 및 final 메서드이므로 다른 클래스에서 extends한 후에 override 불가.
//            MockHttpSession mockSession = new MockHttpSession();
//            mockSession.setAttribute("SPRING_SECURITY_LAST_EXCEPTION", "error");
//
//            // when & then
//             mockMvc.perform(post("/api/users/login")
//                             .session(mockSession)
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(loginRequest)))
//                    .andExpect(status().isOk())
//                    .andExpect(header().string("Authorization", "Bearer " + accessToken));
//
//            assertThat(mockSession.getAttribute("SPRING_SECURITY_LAST_EXCEPTION"))
//                    .isNull(); // super.clearAuthenticationAttributes (request); 내부 동작 검증
//            verify(tokenManagementService).saveRefreshToken(any(), any());
//            //리프레시 토큰은 mockBean으로 해당 함수가 호출되었는지 동작만 검증 가능. MockBean이기 때문에 메서드 내부 로직이 실행 안됨
//            verify(tokenManagementService).addRefreshTokenToCookie(any(), any(), any());
//        }
//    }
//
//    @Nested
//    @DisplayName("로그아웃 API 테스트")
//    class Logout {
//
//        @Test
//        @DisplayName("정상적인 요청인 경우 로그아웃 성공")
//        void logout_success() throws Exception {
//            // given
//            String token = "validToken";
//            Long userId = 1L;
//            String authorizationHeader = "Bearer " + token;
//
//            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
//                when(tokenProvider.getUserId(token)).thenReturn(userId);
//
//                // when & then
//                MvcResult result = mockMvc.perform(post("/api/users/logout")
//                                .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
//                        .andExpect(status().isOk())
//                        .andReturn();
//
//                MockHttpServletResponse response = result.getResponse();
//                assertThat(response.getContentAsString()).isEqualTo("Successfully logged out.");
//
//
//                // 정적 메소드 검증 (Mockito-inline 방식)
//                cookieUtilMock.verify(() ->
//                        CookieUtil.deleteCookie(any(HttpServletRequest.class),
//                                any(HttpServletResponse.class),
//                                eq("refresh_token"))
//                );
//
//                cookieUtilMock.verify(() ->
//                        CookieUtil.deleteCookie(any(HttpServletRequest.class),
//                                any(HttpServletResponse.class),
//                                eq("access_token"))
//                );
//
//                verify(refreshTokenService).deleteRefreshToken(userId);
//            }
//        }
//
//
//        @Test
//        @DisplayName("만료된 토큰으로 로그아웃 요청시 성공")
//        void logout_with_expired_token() throws Exception {
//            // given
//            String expiredToken = "expiredToken";
//            String authorizationHeader = "Bearer " + expiredToken;
//            Long userId = 1L;
//
//            // 만료된 토큰 예외 발생 시뮬레이션
//            Claims claims = Jwts.claims()
//                    .add("id", userId)
//                    .build();
//
//            when(tokenProvider.getUserId(expiredToken))
//                    .thenThrow(new ExpiredJwtException(null, claims, "토큰 만료"));
//
//            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
//                // when & then
//                MvcResult result = mockMvc.perform(post("/api/users/logout")
//                                .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
//                        .andExpect(status().isOk())
//                        .andExpect(content().string("Successfully logged out with expired token."))
//                        .andReturn();
//
//                // 쿠키 삭제 검증
//                cookieUtilMock.verify(() ->
//                        CookieUtil.deleteCookie(any(HttpServletRequest.class),
//                                any(HttpServletResponse.class),
//                                eq("refresh_token"))
//                );
//
//                cookieUtilMock.verify(() ->
//                        CookieUtil.deleteCookie(any(HttpServletRequest.class),
//                                any(HttpServletResponse.class),
//                                eq("access_token"))
//                );
//
//                verify(refreshTokenService).deleteRefreshToken(userId);
//            }
//        }
//
//        @Test
//        @DisplayName("유효하지 않은 토큰으로 로그아웃 요청시 실패")
//        void logout_with_invalid_token() throws Exception {
//            // given
//            String invalidToken = "invalidToken";
//            String authorizationHeader = "Bearer " + invalidToken;
//
//            when(tokenProvider.getUserId(invalidToken))
//                    .thenThrow(new JwtException("유효하지 않은 토큰"));
//
//            try (MockedStatic<CookieUtil> cookieUtilMock = Mockito.mockStatic(CookieUtil.class)) {
//                // when & then
//                MvcResult result = mockMvc.perform(post("/api/users/logout")
//                                .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
//                        .andExpect(status().isUnauthorized())
//                        .andExpect(content().string("Invalid token."))
//                        .andReturn();
//
//                // 쿠키는 삭제되어야 함
//                cookieUtilMock.verify(() ->
//                        CookieUtil.deleteCookie(any(HttpServletRequest.class),
//                                any(HttpServletResponse.class),
//                                eq("refresh_token"))
//                );
//
//                cookieUtilMock.verify(() ->
//                        CookieUtil.deleteCookie(any(HttpServletRequest.class),
//                                any(HttpServletResponse.class),
//                                eq("access_token"))
//                );
//
//                // refreshToken은 삭제되지 않아야 함
//                verify(refreshTokenService, never()).deleteRefreshToken(any());
//            }
//        }
//
//    }
//
//    @Nested
//    @DisplayName("사용자 존재 여부 확인 API 테스트")
//    class UserAvailabilityTest {
//
//        @Test
//        @DisplayName("존재하는 블로그 아이디 조회 시 200 OK 반환 및 이미 사용중인 블로그 아이디 메시지 응답")
//        void checkExistingBlogId() throws Exception {
//            // given
//            String blogId = "existingBlogId";
//            when(userService.existsByBlogId(blogId)).thenReturn(true);
//
//            // when & then
//            mockMvc.perform(get("/api/check/blogId/{blogId}", blogId))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.data").value(true))
//                    .andExpect(jsonPath("$.message").value("이미 사용중인 블로그 아이디 입니다."))
//                    .andDo(print());
//
//            verify(userService).existsByBlogId(blogId);
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 블로그 아이디 조회 시 200 OK 반환 및 사용 가능한 블로그 아이디 메시지 응답")
//        void checkNonExistingBlogId() throws Exception {
//            // given
//            String blogId = "nonExistingBlogId";
//            when(userService.existsByBlogId(blogId)).thenReturn(false);
//
//            // when & then
//            mockMvc.perform(get("/api/check/blogId/{blogId}", blogId))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.data").value(false))
//                    .andExpect(jsonPath("$.message").value("사용 가능한 블로그 아이디 입니다"))
//                    .andDo(print());
//
//            verify(userService).existsByBlogId(blogId);
//        }
//// 아래는 나주엥
////        @Test
////        @DisplayName("캐시 무효화 요청 시 204 No Content 반환")
////        void invalidateUserCache() throws Exception {
////            // given
////            String userIdentifier = "testUser";
////
////            // when & then
////            mockMvc.perform(delete("/api/{userIdentifier}/availability/invalidation", userIdentifier))
////                    .andExpect(status().isNoContent())
////                    .andDo(print());
////
////            verify(userService).invalidateUserCache(userIdentifier);
////        }
//    }
//
//}