package com.yhs.blog.springboot.jpa.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.security.config.WebOAuthFormJwtSecurityConfig;
import com.yhs.blog.springboot.jpa.security.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.security.dto.response.SignUpUserResponse;
import com.yhs.blog.springboot.jpa.security.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.security.jwt.service.RefreshTokenService;
import com.yhs.blog.springboot.jpa.security.jwt.service.TokenManagementService;
import com.yhs.blog.springboot.jpa.security.jwt.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockBean(JpaMetamodelMappingContext.class) // JPA Auditing 관련 설정 무시
class UserApiControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private TokenProvider tokenProvider;
    @MockBean
    private RefreshTokenService refreshTokenService;
    @MockBean
    private TokenManagementService tokenManagementService;
    @MockBean
    private TokenService tokenService;


    @Nested
    @DisplayName("회원가입 API 테스트")
    class SignUp {


        @Test
        @DisplayName("정상적인 요청이면 회원가입이 성공한다")
        void signup_success() throws Exception {
            // given
            SignUpUserRequest request = new SignUpUserRequest(
                    "testUser",
                    "test@example.com",
                    "password123"
            );

            SignUpUserResponse response = new SignUpUserResponse(
                    1L,
                    "testUser",
                    "test",
                    "test@example.com"
            );

            when(userService.createUser(any(SignUpUserRequest.class))).thenReturn(response);

            // when & then
            mockMvc.perform(post("/api/users/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.username").value("testUser"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.userIdentifier").value("test"))
                    .andExpect(jsonPath("$.message").value("User created successfully."));
        }

    }

}