package com.yhs.blog.springboot.jpa.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yhs.blog.springboot.jpa.aop.ratelimit.RateLimit;
import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Log4j2
@RestController
@RequiredArgsConstructor
public class UserApiController extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final TokenManagementService tokenManagementService;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    // 회원가입시 이메일 인증코드 전송, 인증코드 재전송 부분 공통 처리
    @PostMapping("/api/users/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody @Valid SignUpUserRequest signUpUserRequest) throws JsonProcessingException {

        RateLimitResponse result = emailService.processEmailVerification(signUpUserRequest);
        if (result.isSuccess()) {
            return ResponseEntity.status(result.getStatusCode())
                    .body(new SuccessResponse<>(result.getData(), result.getMessage()));
        }


        return ResponseEntity.status(result.getStatusCode())
                .body(new ErrorResponse(result.getMessage(), result.getStatusCode()));

    }

    @PostMapping("/api/users/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestBody @Valid VerifyEmailRequest verifyEmailRequest) {

        RateLimitResponse result = emailService.completeVerification(verifyEmailRequest);

        log.info("result: " + result);

        if (result.isSuccess()) {
            return ResponseEntity.status(result.getStatusCode())
                    .body(new SuccessResponse<>(result.getData(), result.getMessage()));
        }

        return ResponseEntity.status(result.getStatusCode())
                .body(new ErrorResponse(result.getMessage(), result.getStatusCode()));
    }

    @RateLimit(key = "Login")
    @PostMapping("/api/users/login")
    @Transactional // saveRefreshToken DB작업 있어서 트랜잭션 추가해야함
    public ResponseEntity<SuccessResponse<Void>> login(@RequestBody @Valid LoginRequest loginRequest,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws ServletException, IOException {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContextHolder에 인증 정보 저장

//        SecurityContextHolder에 저장되어 있는 사용자 주체를 꺼내옴
        User user = (User) authentication.getPrincipal();

        HttpHeaders headers = new HttpHeaders();

        String refreshToken;

        if (loginRequest.getRememberMe()) {
            refreshToken = tokenProvider.generateToken(user,
                    TokenManagementService.REMEMBER_ME_REFRESH_TOKEN_DURATION);
            redisTemplate.opsForValue().set(TokenManagementService.RT_PREFIX + user.getEmail(), refreshToken,
                    TokenManagementService.REMEMBER_ME_REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        } else {
            refreshToken = tokenProvider.generateToken(user, TokenManagementService.REFRESH_TOKEN_DURATION);

            redisTemplate.opsForValue().set(TokenManagementService.RT_PREFIX + user.getEmail(), refreshToken,
                    TokenManagementService.REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        }

        // 생성된 리프레시 토큰을 클라이언트측 쿠키에 저장 -> 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하기 위함
        tokenManagementService.addRefreshTokenToCookie(request, response, refreshToken, loginRequest.getRememberMe());

        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenManagementService.ACCESS_TOKEN_DURATION);

        // 응답 헤더에 액세스 토큰 추가
        headers.set("Authorization", "Bearer " + accessToken);

        //  인증 실패와 관련된 정보를 세션에서 제거. 즉 다음에 재로그인할때 만약 이전 인증 실패 정보가 남아있다면 이전 인증 실패 정보가 남아있지 않도록 함.
        super.clearAuthenticationAttributes(request);

        return ResponseEntity.ok().headers(headers).body(new SuccessResponse<>("로그인에 성공하였습니다."));

    }


    // Custom logout 로직을 구현한 경우 시큐리티에서 제공하는 logout을 사용하지 않는다.
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
            // tokenProvider.gerUserId()에서 내부적으로 만료된 토큰인지 유효성 검사를 함. 이때 만료된 토큰이면 아래 ExpiredJwtException Catch문으로 넘어간다.
            String email = tokenProvider.getEmail(token);

            redisTemplate.delete(TokenManagementService.RT_PREFIX + email);

            return ResponseEntity.ok("로그아웃에 성공하였습니다.");

        } catch (ExpiredJwtException e) {
            // 만료된 토큰일 때도 userId를 추출 가능 (ExpiredJwtException을 통해 Claims에 접근) 즉
            // ExpiredJwtException을 통해 만료된 토큰에 있는 Claims에 접근한다.
            String email = tokenProvider.getEmail(token);
            redisTemplate.delete(TokenManagementService.RT_PREFIX + email);
            return ResponseEntity.ok("로그아웃에 성공하였습니다.");

        } catch (Exception e) {
            log.error("Error deleting refresh token for userId: ", e);
            // 유효하지 않은 토큰(서명이 잘못되거나 변조된 경우 등 즉 비정상적인 토큰일 경우)이면 거부 한다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

    }
//
//    @GetMapping("/api/{userIdentifier}/availability")
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

    @GetMapping("/api/check/blog-id/exists/{blogId}")
    public ResponseEntity<ApiResponse> checkExistsBlogId(@PathVariable("blogId") String blogId) {

        if (userService.isExistsBlogId(blogId)) {
            return ResponseEntity.ok()
                    .body(new SuccessResponse<>("User exists"));
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("User not found.", 404));
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
    @GetMapping("/api/check/blog-id/duplicate/{blogId}")
    public ResponseEntity<ApiResponse> checkDuplicateBlogId(@PathVariable("blogId") String blogId) {
        
        DuplicateCheckResponse response = userService.isDuplicateBlogId(blogId);

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
    @GetMapping("/api/check/email/duplicate/{email}")
    public ResponseEntity<ApiResponse> checkDuplicateEmail(@PathVariable("email") String email) {

        DuplicateCheckResponse response = userService.isDuplicateEmail(email);

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
    @Parameter(name = "username", description = "확인할 사용자명", required = true)
    @GetMapping("/api/check/username/duplicate/{username}")
    public ResponseEntity<ApiResponse> checkDuplicateUsername(@PathVariable("username") String username) {


        DuplicateCheckResponse response = userService.isDuplicateUsername(username);

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
