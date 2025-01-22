package com.yhs.blog.springboot.jpa.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.aop.ratelimit.RateLimit;
import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenService;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.UserSettingsRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.VerifyEmailRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.*;
import com.yhs.blog.springboot.jpa.domain.user.service.AuthenticationService;
import com.yhs.blog.springboot.jpa.domain.user.service.EmailService;
import com.yhs.blog.springboot.jpa.domain.user.service.LogoutProcessService;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.web.cookie.TokenCookieManager;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;

import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Log4j2
@RestController
@RequiredArgsConstructor
public class UserController extends SimpleUrlAuthenticationSuccessHandler {

        private final UserService userService;
        private final TokenCookieManager TokenCookieManager;
        private final EmailService emailService;
        private final LogoutProcessService logoutProcessService;
        private final AuthenticationService authenticationService;

        // 회원가입시 이메일 인증코드 전송, 인증코드 재전송 부분 공통 처리
        @PostMapping("/api/users/signup")
        public ResponseEntity<ApiResponse> signup(@RequestBody @Valid SignUpUserRequest signUpUserRequest)
                        throws JsonProcessingException {

                // 수정 필요 인증코드 전송 응답 SignUpUserRequest안에 너무 불필요하고 민감한 정보가 들어가 있음
                RateLimitResponse<SignUpUserRequest> result = emailService.processEmailVerification(signUpUserRequest);
                if (result.isSuccess()) {
                        return ResponseEntity.status(result.getStatusCode())
                                        .body(new SuccessResponse<>(result.getData(), result.getMessage()));
                }

                return ResponseEntity.status(result.getStatusCode())
                                .body(new ErrorResponse(result.getMessage(), result.getStatusCode()));

        }

        @PostMapping("/api/users/verify-email")
        public ResponseEntity<ApiResponse> verifyEmail(@RequestBody @Valid VerifyEmailRequest verifyEmailRequest) {

                RateLimitResponse<SignUpUserResponse> result = emailService.completeVerification(verifyEmailRequest);

                if (result.isSuccess()) {
                        return ResponseEntity.status(result.getStatusCode())
                                        .body(new SuccessResponse<>(result.getData(), result.getMessage()));
                }

                return ResponseEntity.status(result.getStatusCode())
                                .body(new ErrorResponse(result.getMessage(), result.getStatusCode()));
        }

        @Loggable
        @RateLimit(key = "Login")
        @PostMapping("/api/users/login")
        @Transactional // saveRefreshToken DB작업 있어서 트랜잭션 추가해야함
        public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest loginRequest,
                        HttpServletRequest request,
                        HttpServletResponse response) throws ServletException, IOException {

                // 스프링에서 제공하는 User가 아닌 Entity 유저
                // RateLimitAspect에서 인증 실패에 관한 예외 처리 진행
                User user = authenticationService.authenticateUser(loginRequest);

                HttpHeaders headers = new HttpHeaders();

                LoginResultToken loginResultToken = userService.getTokenForLoginUser(user, loginRequest);

                // 생성된 리프레시 토큰을 클라이언트측 쿠키에 저장 -> 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하기 위함
                TokenCookieManager.addRefreshTokenToCookie(request, response, loginResultToken.refreshToken(),
                                loginRequest.getRememberMe());

                // 응답 헤더에 액세스 토큰 추가
                headers.set("Authorization", "Bearer " + loginResultToken.accessToken());

                // 인증 실패와 관련된 정보를 세션에서 제거. 즉 다음에 재로그인할때 만약 이전 인증 실패 정보가 남아있다면 이전 인증 실패 정보가
                // 남아있지않도록 함.
                super.clearAuthenticationAttributes(request);

                return ResponseEntity.ok().headers(headers)
                                .body(new SuccessResponse<>("로그인에 성공하였습니다."));
        }

        // Custom logout 로직을 구현한 경우 시큐리티에서 제공하는 logout을 사용하지 않는다.
        // 토큰이 변조되지만 않고 시간상 만료되어도 로그아웃 처리하기 위해 jwt 필터 사용하지 않고 여기서 처리
        @PostMapping("/api/users/logout")
        @Transactional // deleteRefreshToken DB작업 있어서 트랜잭션 추가해야함
        public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {

                String authorizationHeader = request.getHeader("Authorization");

                if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 상태가 아닙니다.");
                }

                // "Bearer " 이후의 토큰 값만 추출
                String token = authorizationHeader.substring(7);

                CookieUtil.deleteCookie(request, response, "refresh_token");
                CookieUtil.deleteCookie(request, response, "access_token");

                try {
                        // logoutProcessService.logoutUse(token)에서 내부적으로 만료된 토큰인지 유효성 검사를 함. 이때 만료된 토큰이면
                        // 아래
                        // ExpiredJwtException Catch문으로 넘어간다.
                        logoutProcessService.logoutUser(token);
                        return ResponseEntity.ok("로그아웃에 성공하였습니다.");

                } catch (ExpiredJwtException e) {
                        // 만료된 토큰일 때도 userId를 추출 가능 (ExpiredJwtException을 통해 Claims에 접근) 즉
                        // ExpiredJwtException을 통해 만료된 토큰에 있는 Claims에 접근한다.
                        logoutProcessService.logoutUserByExpiredToken(e);
                        return ResponseEntity.ok("로그아웃에 성공하였습니다.");

                } catch (Exception e) {
                        log.error("Error deleting refresh token for userId: ", e);
                        // 유효하지 않은 토큰(서명이 잘못되거나 변조된 경우 등 즉 비정상적인 토큰일 경우)이면 거부 한다.
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
                }

        }

        /* formData를 객체에 바인딩하기 위해서 RequestBody 대신 ModelAttribute 사용 한다 */
        @PatchMapping("/api/users/{blogId}/settings")
        @PreAuthorize("#userBlogId == authentication.name")
        public ResponseEntity<ApiResponse> updateSettings(
                        @P("userBlogId") @PathVariable("blogId") String blogId,
                        @ModelAttribute @Valid UserSettingsRequest settingsRequest) {
                try {
                        userService.updateUserSettings(blogId, settingsRequest);
                        return ResponseEntity.ok()
                                        .body(new SuccessResponse<>("사용자 설정이 성공적으로 업데이트되었습니다."));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                                        "사용자 프로필 업데이트 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value()));
                }

        }

        /**
         * 주어진 블로그 ID를 기반으로 사용자의 프로필 정보를 조회한다
         * 
         * @param blogId 프로필을 조회할 블로그의 ID
         * @return 사용자의 프로필 정보를 포함한 ResponseEntity
         */
        @GetMapping("/api/users/{blogId}/profile")
        public ResponseEntity<ApiResponse> getUserProfilePublic(@PathVariable("blogId") String blogId) {
                UserPublicProfileResponse publicUserProfile = userService.findUserByBlogIdAndConvertDTO(blogId);
                return ResponseEntity.ok().body(new SuccessResponse<>(publicUserProfile, "공개 사용자 정보 조회를 성공하였습니다."));
        }

        // 토큰 필터에서 인증 검사 끝나서 여기서 isAuthenticaed()필요x
        @GetMapping("/api/users/profile/private")
        public ResponseEntity<ApiResponse> getUserProfilePrivate(HttpServletRequest request,
                        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

                String blogId = user.getUsername();

                UserPrivateProfileResponse privateUserProfile = userService.findUserByTokenAndByBlogId(blogId);
                return ResponseEntity.ok().body(new SuccessResponse<>(privateUserProfile, "비공개 사용자 정보 조회를 성공하였습니다."));
        }

        @GetMapping("/api/check/blog-id/exists/{blogId}")
        public ResponseEntity<ApiResponse> checkExistsBlogId(@PathVariable("blogId") String blogId) {

                if (userService.isExistsBlogId(blogId)) {
                        return ResponseEntity.ok()
                                        .body(new SuccessResponse<>(blogId + " 사용자가 존재 합니다."));
                }

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(new ErrorResponse(blogId + " 사용자를 조회할 수 없습니다.", 404));
        }

        @Operation(summary = "블로그 ID 중복 확인")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "중복 확인 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "너무 많은 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하고 있는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @Parameter(name = "blogId", description = "확인할 블로그 ID", required = true)
        @GetMapping("/api/check/blog-id/duplicate/{blogId}")
        public ResponseEntity<ApiResponse> checkDuplicateBlogId(@PathVariable("blogId") String blogId) {

                DuplicateCheckResponse response = userService.isDuplicateBlogId(blogId);

                return checkDuplicate(response);
        }

        @Operation(summary = "이메일 중복 확인")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "중복 확인 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "너무 많은 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하고 있는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @Parameter(name = "email", description = "확인할 이메일", required = true)
        @GetMapping("/api/check/email/duplicate/{email}")
        public ResponseEntity<ApiResponse> checkDuplicateEmail(@PathVariable("email") String email) {

                DuplicateCheckResponse response = userService.isDuplicateEmail(email);

                return checkDuplicate(response);

        }

        @Operation(summary = "사용자명 중복 확인")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "중복 확인 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "너무 많은 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하고 있는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @Parameter(name = "username", description = "확인할 사용자명", required = true)
        @GetMapping("/api/check/username/duplicate/{username}")
        public ResponseEntity<ApiResponse> checkDuplicateUsername(@PathVariable("username") String username) {

                DuplicateCheckResponse response = userService.isDuplicateUsername(username);

                return checkDuplicate(response);
        }

        private ResponseEntity<ApiResponse> checkDuplicate(DuplicateCheckResponse response) {

                if (response.isExist()) {
                        // 이미 존재하는 경우
                        return ResponseEntity.status(HttpStatus.CONFLICT) // 409
                                        .body(new ErrorResponse(response.getMessage(), HttpStatus.CONFLICT.value()));
                }

                return ResponseEntity.ok(new SuccessResponse<>(response.isExist(), response.getMessage()));
        }

}
