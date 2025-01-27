package com.yhs.blog.springboot.jpa.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yhs.blog.springboot.jpa.common.response.BaseResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.VerifyEmailRequest;
import com.yhs.blog.springboot.jpa.domain.user.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Tag(name = "사용자 등록", description = "회원가입, 이메일 인증 등 사용자 등록 관련 API")
@Log4j2
@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserRegistrationController {

    
        private final EmailService emailService;

    // 회원가입시 이메일 인증코드 전송, 인증코드 재전송 부분 공통 처리
        // 불필요한 응답DTO 삭제 후 성능 향상
        @Operation(summary = "회원가입 요청 후 이메일로 인증코드 발송", description = "사용자가 회원가입 요청 후 이메일로 인증코드를 발송")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "이메일 인증코드 발송 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @PostMapping("/signup")
        public ResponseEntity<BaseResponse> signup(@RequestBody @Valid SignUpUserRequest signUpUserRequest)
                        throws JsonProcessingException {

                log.info("[UserRegistrationController] signup() 요청");

                // 수정 필요 인증코드 전송 응답 SignUpUserRequest안에 너무 불필요하고 민감한 정보가 들어가 있음
                RateLimitResponse<Void> result = emailService.processEmailVerification(signUpUserRequest);
                if (result.isSuccess()) {

                        log.info("[UserRegistrationController] signup() result.isSuccess(): true 성공 분기 응답");

                        return ResponseEntity.status(result.getStatusCode())
                                        .body(new SuccessResponse<>(result.getMessage()));
                }

                log.info("[UserRegistrationController] signup() 실패 응답 - result: {}", result);

                return ResponseEntity.status(result.getStatusCode())
                                .body(new ErrorResponse(result.getMessage(), result.getStatusCode()));

        }

        @Operation(summary = "인증 코드 검증", description = "사용자가 이메일로 전송받은 인증 코드를 최종적으로 검증")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "인증 코드 검증 성공(사용자 계정 생성 성공)", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "400", description = "인증 코드 번호 잘못 전송(잘못된 요청)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "410", description = "만료된 인증 코드", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "429", description = "너무 많은 요청(1분에 최대 3회)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        // 불필요한 응답DTO 삭제 후 성능 향상
        @PostMapping("/verify-code")
        public ResponseEntity<BaseResponse> verifyCode(@RequestBody @Valid VerifyEmailRequest verifyEmailRequest) {

                log.info("[UserRegistrationController] verifyEmail() 요청");

                RateLimitResponse<Void> result = emailService.completeVerification(verifyEmailRequest);

                if (result.isSuccess()) {

                        log.info("[UserRegistrationController] verifyEmail() result.isSuccess(): true 성공 분기 응답");

                        return ResponseEntity.status(result.getStatusCode())
                                        .body(new SuccessResponse<>(result.getMessage()));
                }

                log.info("[UserRegistrationController] verifyEmail() 실패 응답");

                return ResponseEntity.status(result.getStatusCode())
                                .body(new ErrorResponse(result.getMessage(), result.getStatusCode()));
        }
}
