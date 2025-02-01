package com.yhs.blog.springboot.jpa.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.common.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.VerifyEmailRequest;
import com.yhs.blog.springboot.jpa.domain.user.service.EmailService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRegistrationController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 모든 필터 무시
public class UserRegistrationControllerUnitTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private EmailService emailService;

        @MockitoBean
        private JpaMetamodelMappingContext jpaMetamodelMappingContext;

        @Test
        @DisplayName("정상적인 요청이면 회원가입 시 이메일로 인증코드 발송 성공")
        void 회원가입_요청시_이메일_인증코드_발송_성공_테스트() throws Exception {
                // given
                SignUpUserRequest request = new SignUpUserRequest(
                                "test__",
                                "테스트123",
                                "test@example.com",
                                "TestPassword123*",
                                "TestPassword123*");

                RateLimitResponse<Void> expectedResponse = new RateLimitResponse<>(true, "이메일 인증 코드가 발송되었습니다.",
                                200, null);

                when(emailService.processEmailVerification(any(SignUpUserRequest.class))).thenReturn(expectedResponse);
                // when & then
                mockMvc.perform(post("/api/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("이메일 인증 코드가 발송되었습니다."));

                verify(emailService).processEmailVerification(any(SignUpUserRequest.class));
        }

        @Test
        @DisplayName("이메일로 인증코드 검증 성공")
        void 이메일_인증코드_검증_성공_테스트() throws Exception {
                // given
                VerifyEmailRequest request = new VerifyEmailRequest("test_blog_id", "123456");

                RateLimitResponse<Void> expectedResponse = new RateLimitResponse<>(true, "회원가입에 성공하였습니다.",
                                201, null);

                when(emailService.completeVerification(any(VerifyEmailRequest.class))).thenReturn(expectedResponse);
                // when & then
                mockMvc.perform(post("/api/users/verify-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("회원가입에 성공하였습니다."));

                verify(emailService).completeVerification(any(VerifyEmailRequest.class));
        }

}
