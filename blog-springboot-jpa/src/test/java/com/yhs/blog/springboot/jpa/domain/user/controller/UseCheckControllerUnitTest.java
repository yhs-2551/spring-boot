package com.yhs.blog.springboot.jpa.domain.user.controller;

import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import com.yhs.blog.springboot.jpa.domain.user.service.UserCheckService;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest + MockMVC를 사용한 컨트롤러 단위 테스트
@WebMvcTest(UserCheckController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 모든 필터 무시
class UseCheckControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext; // JPA Auditing 스터빙하기 위해 필요. 없으면 Application context
                                                                   // fail. 즉 단순히 컨텍스트 로딩을 위한 더미 객체

    // @Autowired
    // private ObjectMapper objectMapper;

    @MockitoBean
    private UserCheckService userCheckService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    // @MockBean
    // private TokenProvider tokenProvider;

    // @MockBean
    // private RefreshTokenService refreshTokenService;

    // @MockBean
    // private TokenManagementService tokenManagementService;

    // @MockBean
    // private TokenService tokenService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Nested
    @DisplayName("중복확인 ")
    class UserAvailabilityTest {

        @Test
        @DisplayName("존재하는 블로그 아이디 조회 시 409충돌 반환 및 이미 사용중인 블로그 아이디 메시지 응답")
        void 존재하는_블로그ID_중복확인_테스트() throws Exception {
            // given
            String blogId = "existingBlogId";
            DuplicateCheckResponse expectedResponse = new DuplicateCheckResponse(true, "이미 사용중인 블로그 아이디 입니다.");

            when(userCheckService.isDuplicateBlogId(blogId))
                    .thenReturn(expectedResponse);

            // when & then
            mockMvc.perform(get("/api/check/blog-id/duplicate/{blogId}", blogId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("이미 사용중인 블로그 아이디 입니다."))
                    .andExpect(jsonPath("$.errorCode").value(409)) // ErrorResponse의 status 필드 검증
                    .andDo(print());

            verify(userCheckService).isDuplicateBlogId(blogId);
        }

        @Test
        @DisplayName("존재하지 않는 블로그 아이디 조회 시 200 OK 반환 및 사용 가능한 블로그 아이디 메시지 응답")
        void 존재하지_않는_블로그ID_중복확인_테스트() throws Exception {
            // given
            String blogId = "nonExistingBlogId";
            DuplicateCheckResponse expectedResponse = new DuplicateCheckResponse(false, "사용 가능한 블로그 아이디 입니다.");

            when(userCheckService.isDuplicateBlogId(blogId)).thenReturn(expectedResponse);

            // when & then
            mockMvc.perform(get("/api/check/blog-id/duplicate/{blogId}", blogId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("사용 가능한 블로그 아이디 입니다."))
                    .andDo(print());

            verify(userCheckService).isDuplicateBlogId(blogId);
        }

    }

}