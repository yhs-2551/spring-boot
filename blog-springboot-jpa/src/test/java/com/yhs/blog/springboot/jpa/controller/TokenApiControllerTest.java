package com.yhs.blog.springboot.jpa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.config.jwt.JwtFactory;
import com.yhs.blog.springboot.jpa.config.jwt.JwtProperties;
import com.yhs.blog.springboot.jpa.dto.CreateAccessTokenRequest;
import com.yhs.blog.springboot.jpa.entity.RefreshToken;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.repository.RefreshTokenRepository;
import com.yhs.blog.springboot.jpa.repository.UserRepository;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @DisplayName("CreateNewAccessToken: 새로운 엑세스 토큰을 발급한다.")
    @Test
    @Transactional
    public void createNewAccessToken() throws Exception {

//        given
        final String url = "/api/token";

        User testUser =
                userRepository.save(User.builder().username("testUser").email("user@gmail.com").password(
                        "test").build());

        String refreshToken =
                JwtFactory.builder().claims(Map.of("id", testUser.getId())).build().createToken(jwtProperties);

        // 사용자 생성 및 refreshToken 생성(테스트에선 액세스 토큰 생성 방식과 동일하게 해도 무방) 이후 Refresh 토큰을 DB에 저장
        refreshTokenRepository.save(new RefreshToken(testUser.getId(), refreshToken));

        // 요청을 할때 요청 refresh token과 db에 저장되어 있는 refresh token의 유효성 검사를 할 예정.
        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setRefreshToken(refreshToken);
        final String requestBody = objectMapper.writeValueAsString(request);

//        when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));

//        then, HTTP Stataus Code가 Created 면서, CreateAccessTokenResponse의 accessToken 필드가 빈값이 아니면
//        테스트 통과
        resultActions.andExpect(status().isCreated()).andExpect(jsonPath("$.accessToken").isNotEmpty());

    }
}
