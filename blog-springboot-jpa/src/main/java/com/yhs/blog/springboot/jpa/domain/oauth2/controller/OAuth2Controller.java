package com.yhs.blog.springboot.jpa.domain.oauth2.controller;

import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.OAuth2SignUpResponse;
import com.yhs.blog.springboot.jpa.domain.oauth2.service.OAuth2SignUpService;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenCookieManager;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OAuth2Controller {

        private final OAuth2SignUpService oAuth2SignUpService;
        private final TokenCookieManager TokenCookieManager;

        @PostMapping("/oauth2/users")
        public ResponseEntity<ApiResponse> oAuth2UserSignup(
                        @Valid @RequestBody AdditionalInfoRequest request,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse) {

                try {
                        RateLimitResponse<OAuth2SignUpResponse> rateLimitResponse = oAuth2SignUpService
                                        .processOAuth2SignUp(request);

                        if (!rateLimitResponse.isSuccess()) {
                                return createRateLimitErrorResponse();
                        }

                        return createSuccessResponse(
                                        (OAuth2SignUpResponse) rateLimitResponse.getData(),
                                        httpRequest,
                                        httpResponse);

                } catch (Exception e) {
                        log.error("OAuth2 회원가입 실패: {}", e.getMessage());
                        return createErrorResponse("OAuth2 회원가입에 실패 하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        private ResponseEntity<ApiResponse> createSuccessResponse(
                        OAuth2SignUpResponse response,
                        HttpServletRequest request,
                        HttpServletResponse servletResponse) {

                TokenCookieManager.addRefreshTokenToCookie(
                                request,
                                servletResponse,
                                response.refreshToken(),
                                response.isRememberMe());

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + response.accessToken());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .headers(headers)
                                .body(new SuccessResponse<>(
                                                response.userInfo(),
                                                "OAuth2 신규 사용자 등록에 성공하였습니다."));
        }

        private ResponseEntity<ApiResponse> createRateLimitErrorResponse() {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                .body(new ErrorResponse(
                                                "너무 많은 시도입니다. 1분 후에 다시 시도해주세요.",
                                                HttpStatus.TOO_MANY_REQUESTS.value()));
        }

        private ResponseEntity<ApiResponse> createErrorResponse(String message, HttpStatus status) {
                return ResponseEntity.status(status)
                                .body(new ErrorResponse(message, status.value()));
        }
}
