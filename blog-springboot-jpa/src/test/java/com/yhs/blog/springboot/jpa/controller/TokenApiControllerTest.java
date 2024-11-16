package com.yhs.blog.springboot.jpa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.config.jwt.JwtFactory;
import com.yhs.blog.springboot.jpa.security.jwt.config.JwtProperties;
import com.yhs.blog.springboot.jpa.domain.token.entity.RefreshToken;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.token.repository.RefreshTokenRepository;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.security.jwt.service.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.Map;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TokenApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    TokenService tokenService;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

//    @DisplayName("CreateNewAccessToken: 새로운 엑세스 토큰을 발급한다.")
//    @Test
//    public void createNewAccessToken() throws Exception {
//
////        given
//        final String url = "/api/token";
//
//        User testUser =
//                userRepository.save(User.builder().username("testUser").email("user@gmail.com").password(
//                        "test").build());
//
//        String refreshToken =
//                JwtFactory.builder().claims(Map.of("id", testUser.getId())).build().createToken(jwtProperties);
//
//        // 사용자 생성 및 refreshToken 생성(테스트에선 액세스 토큰 생성 방식과 동일하게 해도 무방) 이후 Refresh 토큰을 DB에 저장
//        refreshTokenRepository.save(new RefreshToken(testUser.getId(), refreshToken));
//
//         요청을 할때 요청 refresh token과 db에 저장되어 있는 refresh token의 유효성 검사를 할 예정.
//        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
//        request.setRefreshToken(refreshToken);
//        final String requestBody = objectMapper.writeValueAsString(request);
//
//        when
//        ResultActions resultActions = mockMvc.perform(post(url)
//                .contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));
//
//        then, HTTP Stataus Code가 Created 면서, CreateAccessTokenResponse의 accessToken 필드가 빈값이 아니면
//        테스트 통과
//        resultActions.andExpect(status().isCreated()).andExpect(jsonPath("$.accessToken").isNotEmpty());
//
//    }

    @DisplayName("createReIssueAccessTokenByRefreshToken: reissue new access token using vaild " +
            "refresh " +
            "token")
    @Test
    public void createReIssueAccessTokenByRefreshToken() throws Exception {
        final String url = "/api/token/new-token";

        User testUser = userRepository.save(User.builder().username("testUser").email("user@gmail" +
                ".com").password("test").build());

        String refreshToken = JwtFactory.builder().claims(Map.of("id", testUser.getId())).build().createToken(jwtProperties);
        refreshTokenRepository.save(new RefreshToken(testUser.getId(), refreshToken));

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");

        ResultActions resultActions =
                mockMvc.perform(post(url).cookie(refreshTokenCookie).contentType(MediaType.APPLICATION_JSON_VALUE));

        resultActions.andExpect(status().isOk()).andExpect(header().string("Authorization",
                org.hamcrest.Matchers.startsWith("Bearer "))).andExpect(content().string("New access token sent in headers."));
    }


    @DisplayName("Expired refresh token: Issue new refresh and access tokens using expired access token")
    @Test
    @Transactional
    public void createNewTokensWithExpiredAccessToken() throws Exception {
        final String url = "/api/token/new-token";

        // Create and save a test user
        User testUser = userRepository.save(User.builder().username("testUser").email("user@gmail.com").password("test").build());

        // Generate an expired access token for the test user
        String expiredAccessToken = JwtFactory.builder()
                .claims(Map.of("id", testUser.getId()))
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Set expiration to past
                // time
                .build()
                .createToken(jwtProperties);

        // Simulate the request with the expired access token in the header
        ResultActions resultActions = mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + expiredAccessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // Verify the response status, headers, and body
        resultActions.andExpect(status().isOk())
                .andExpect(header().string("Authorization", org.hamcrest.Matchers.startsWith("Bearer ")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.startsWith("refresh_token=")))
                .andExpect(content().string("New access token sent in headers and refresh token added in cookie."));
    }

}
