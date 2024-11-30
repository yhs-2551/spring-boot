package com.yhs.blog.springboot.jpa.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.RefreshTokenService;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenManagementService;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.VerifyEmailRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.EmailService;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
@Log4j2
@RestController
@RequiredArgsConstructor
public class UserApiController extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenManagementService tokenManagementService;
    private final EmailService emailService;

    // 일단 하나씩 해보면서 잘 되는지 회원가입부터 해봐야지
    @PostMapping("/api/users/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody SignUpUserRequest signUpUserRequest) throws JsonProcessingException {
        RateLimitResponse result = emailService.processSignUp(signUpUserRequest);
        if (result.isSuccess()) {
            return ResponseEntity.status(result.getStatusCode())
                    .body(new SuccessResponse<>(result.getMessage()));
        }


        return ResponseEntity.status(result.getStatusCode())
                .body(new ErrorResponse(result.getMessage(), result.getStatusCode()));

    }


    @PostMapping("/api/users/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestBody VerifyEmailRequest verifyEmailRequest) {

        RateLimitResponse result =  emailService.completeVerification(verifyEmailRequest);

        if (result.isSuccess()) {
            return ResponseEntity.status(result.getStatusCode())
                    .body(new SuccessResponse<>(result.getData(), result.getMessage()));
        }

        return ResponseEntity.status(result.getStatusCode())
                .body(new ErrorResponse(result.getMessage(), result.getStatusCode()));
    }

    @PostMapping("/api/users/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws ServletException, IOException {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContextHolder에 인증 정보 저장

//        SecurityContextHolder에 저장되어 있는 사용자 주체를 꺼내옴
        User user = (User) authentication.getPrincipal();

        HttpHeaders headers = new HttpHeaders();


        // 리프레시 토큰 생성
        String refreshToken = tokenProvider.generateToken(user,
                TokenManagementService.REFRESH_TOKEN_DURATION);

        // 리프레시 토큰을 userId와 함께 DB에 저장
        tokenManagementService.saveRefreshToken(user.getId(), refreshToken);

        // 생성된 리프레시 토큰을 클라이언트측 쿠키에 저장 -> 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하기 위함
        tokenManagementService.addRefreshTokenToCookie(request, response, refreshToken);

        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenManagementService.ACCESS_TOKEN_DURATION);

        // 응답 헤더에 액세스 토큰 추가
        headers.set("Authorization", "Bearer " + accessToken);

        //  세션이나 쿠키에 불필요한 데이터가 남아 있지 않도록 하여 보안을 강화함.
        super.clearAuthenticationAttributes(request);

        return ResponseEntity.ok().headers(headers).body("Login Success");

    }

    // Custom logout 로직을 구현한 경우 시큐리티에서 제공하는 logout을 사용하지 않는다.
    @PostMapping("/api/users/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("실행 로그아웃");
        CookieUtil.deleteCookie(request, response, "refresh_token");
        CookieUtil.deleteCookie(request, response, "access_token");

        String authorizationHeader = request.getHeader("Authorization");
        // "Bearer " 이후의 토큰 값만 추출
        String token = authorizationHeader.substring(7);

        try {

            Long userId = tokenProvider.getUserId(token); // tokenProvider.gerUserId()에서 내부적으로
            // 만료된 토큰인지 유효성 검사를 함. 이때 만료된 토큰이면 아래 ExpiredJwtException Catch문으로 넘어간다.
            refreshTokenService.deleteRefreshToken(userId);
            return ResponseEntity.ok("Successfully logged out.");

        } catch (ExpiredJwtException e) {
            // 만료된 토큰일 때도 userId를 추출 가능 (ExpiredJwtException을 통해 Claims에 접근) 즉
            // ExpiredJwtException을 통해 만료된 토큰에 있는 Claims에 접근한다.
            Long userId = e.getClaims().get("id", Long.class);
            refreshTokenService.deleteRefreshToken(userId);
            return ResponseEntity.ok("Successfully logged out with expired token.");

        } catch (Exception e) {
            // 유효하지 않은 토큰(서명이 잘못되거나 변조된 경우 등 즉 비정상적인 토큰일 경우)이면 거부 한다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
        }
    }

    @Operation(summary = "블로그 ID 중복 확인")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "너무 많은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하고 있는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @Parameter(name = "blogId", description = "확인할 블로그 ID", required = true)
    @GetMapping("/api/check/blogId/{blogId}")
    public ResponseEntity<ApiResponse> checkBlogId(@PathVariable("blogId") String blogId) {


        DuplicateCheckResponse response = userService.existsByBlogId(blogId);

        return checkDuplicate(response);
    }

    @Operation(summary = "이메일 중복 확인")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "너무 많은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 존재하고 있는 경우",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    })
    @Parameter(name = "email", description = "확인할 이메일", required = true)
    @GetMapping("/api/check/email/{email}")
    public ResponseEntity<ApiResponse> checkEmail(@PathVariable("email") String email) {

        DuplicateCheckResponse response = userService.existsByEmail(email);

       return checkDuplicate(response);

    }

    @Operation(summary = "사용자명 중복 확인")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "너무 많은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하고 있는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @Parameter(name = "userName", description = "확인할 사용자명", required = true)
    @GetMapping("/api/check/userName/{userName}")
    public ResponseEntity<ApiResponse> checkUserName(@PathVariable("userName") String userName) {


        DuplicateCheckResponse response = userService.existsByUserName(userName);

        return checkDuplicate(response);
    }

    private ResponseEntity<ApiResponse> checkDuplicate(DuplicateCheckResponse response) {

        // 3회 초과 요청이 오면 429 Too Many Requests 응답
        if (response.isLimited()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS) // 429
                    .body(new ErrorResponse(response.getMessage(), HttpStatus.TOO_MANY_REQUESTS.value()));
        }

        if (response.isExist()) {
            // 이미 존재하는 경우
            return ResponseEntity.status(HttpStatus.CONFLICT)  // 409
                    .body(new ErrorResponse(response.getMessage(), HttpStatus.CONFLICT.value()));
        }

        return ResponseEntity.ok(new SuccessResponse<>(response.isExist(), response.getMessage()));
    }

//    // 특정 사용자가 존재하는지 프론트측에서 미들웨어로 확인
//    @GetMapping("/api/check/userIdentifier/{userIdentifier}")
//    public ResponseEntity<ApiResponse> checkUserExists(@PathVariable("userIdentifier") String userIdentifier) {
//
//        log.info("userIdentifier: " + userIdentifier);
//
//        if (userService.existsByUserIdentifier(userIdentifier)) {
//            return ResponseEntity.ok()
//                    .body(new SuccessResponse<>("User exists"));
//        }
//
//        return ResponseEntity
//                .status(HttpStatus.NOT_FOUND)
//                .body(new ErrorResponse("User not found.", 404));
//    }

    // 레디스 캐시 무효화 해야할때:
    // 1) 사용자가 프로필 변경에서 사용자명, BlogId(최초1회만 변경가능)을 변경했을 때
    // 2) 사용자가 회원탈퇴를 했을 때: 사용자명, BlogId, Email 전부 무효화 필요
//    @DeleteMapping("/api/check/userIdentifier/{userIdentifier}/invalidation")
//    public ResponseEntity<Void> invalidateUserCache(@PathVariable String userIdentifier) {
//
//        userService.invalidateUserCache(userIdentifier);
//        return ResponseEntity.noContent().build();
//    }


}
