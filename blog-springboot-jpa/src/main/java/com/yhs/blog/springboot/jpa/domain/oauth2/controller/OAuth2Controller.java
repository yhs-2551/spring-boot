package com.yhs.blog.springboot.jpa.domain.oauth2.controller;

import com.yhs.blog.springboot.jpa.common.response.BaseResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.OAuth2SignUpResponse;
import com.yhs.blog.springboot.jpa.domain.oauth2.service.OAuth2SignUpService;
import com.yhs.blog.springboot.jpa.web.cookie.TokenCookieManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2; 
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OAuth2 신규 사용자 등록", description = "OAuth2 신규 사용자 블로그ID, 사용자명(닉네임)추가 정보 입력 API")
@Log4j2
@RestController
@RequestMapping("/api/oauth2/users")
@RequiredArgsConstructor
public class OAuth2Controller {
        
        private final OAuth2SignUpService oAuth2SignUpService;
        private final TokenCookieManager TokenCookieManager;

        // 불필요한 응답DTO 삭제 후 성능 향상
        @Operation(summary = "OAuth2 신규 사용자 추가 정보 입력", description = "OAuth2 신규 사용자 블로그 ID, 사용자명(닉네임)을 추가 입력 처리 ")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "OAuth2 신규 사용자 등록에 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "429", description = "너무 많은 요청(1분에 최대 3회)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "OAuth2 신규 사용자 추가 정보 입력 요청 - username, blogId, tempOAuth2UserUniqueId(쿼리 파라미터로 넘긴 UUID값) 입력",
                        content = @Content(schema = @Schema(implementation = AdditionalInfoRequest.class)))
        @PostMapping
        public ResponseEntity<BaseResponse> oAuth2UserSignup(
                        @Valid @RequestBody AdditionalInfoRequest request,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse) {

                log.info("[OAuth2Controller] oAuth2UserSignup() 요청");

                try {
                        RateLimitResponse<OAuth2SignUpResponse> rateLimitResponse = oAuth2SignUpService
                                        .processOAuth2SignUp(request);

                        if (!rateLimitResponse.isSuccess()) {

                                log.info("[OAuth2Controller] oAuth2UserSignup() 요청 RateLimitResponse 실패 분기 시작");

                                return createRateLimitErrorResponse();
                        }

                        log.info("[OAuth2Controller] oAuth2UserSignup() 요청 RateLimitResponse 성공 분기 시작");

                        return createSuccessResponse(
                                        (OAuth2SignUpResponse) rateLimitResponse.getData(),
                                        httpRequest,
                                        httpResponse);

                } catch (Exception e) {
                        log.error("OAuth2 회원가입 실패: {}", e.getMessage());
                        return createErrorResponse("OAuth2 회원가입에 실패 하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        private ResponseEntity<BaseResponse> createSuccessResponse(
                        OAuth2SignUpResponse oAuth2SignUpResponse,
                        HttpServletRequest request,
                        HttpServletResponse response) {

                TokenCookieManager.addRefreshTokenToCookie(
                                request,
                                response,
                                oAuth2SignUpResponse.refreshToken(),
                                oAuth2SignUpResponse.isRememberMe());

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + oAuth2SignUpResponse.accessToken());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .headers(headers)
                                .body(new SuccessResponse<>(
                                                "OAuth2 신규 사용자 등록에 성공하였습니다."));
        }

        private ResponseEntity<BaseResponse> createRateLimitErrorResponse() {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                .body(new ErrorResponse(
                                                "너무 많은 시도입니다. 1분 후에 다시 시도해주세요.",
                                                HttpStatus.TOO_MANY_REQUESTS.value()));
        }

        private ResponseEntity<BaseResponse> createErrorResponse(String message, HttpStatus status) {
                return ResponseEntity.status(status)
                                .body(new ErrorResponse(message, status.value()));
        }
}
