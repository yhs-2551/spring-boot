package com.yhs.blog.springboot.jpa.domain.auth.controller;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.response.BaseResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.common.util.cookie.CookieUtil;
import com.yhs.blog.springboot.jpa.domain.auth.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.domain.auth.dto.response.LoginResultToken;
import com.yhs.blog.springboot.jpa.domain.auth.service.LoginProcessService;
import com.yhs.blog.springboot.jpa.domain.auth.service.LogoutProcessService;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser;
import com.yhs.blog.springboot.jpa.web.cookie.TokenCookieManager;

import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Tag(name = "로그인, 로그아웃 처리", description = "로그인, 로그아웃 처리 API")
@Log4j2
@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController extends SimpleUrlAuthenticationSuccessHandler {

    private final LogoutProcessService logoutProcessService;
    private final LoginProcessService loginProcessService;
    private final TokenCookieManager tokenCookieManager;

    @Operation(summary = "로그인 요청 처리", description = "사용자가 로그인 요청 진행")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "401", description = "아이디 및 패스워드가 틀림(로그인 실패)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "너무 많은 요청(1분에 최대 3회)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

    })

    @Loggable
    @PostMapping("/login")
    @Transactional // saveRefreshToken DB작업 있어서 트랜잭션 추가해야함
    public ResponseEntity<BaseResponse> login(@RequestBody @Valid LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        log.info("[AuthController] login() 요청");

        try {
            LoginResultToken loginResultToken = loginProcessService.loginUser(loginRequest);

            HttpHeaders headers = new HttpHeaders();

            // 생성된 리프레시 토큰을 클라이언트측 쿠키에 저장 -> 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하기 위함
            tokenCookieManager.addRefreshTokenToCookie(request, response, loginResultToken.refreshToken(),
                    loginRequest.getRememberMe());

            // 응답 헤더에 액세스 토큰 추가
            headers.set("Authorization", "Bearer " + loginResultToken.accessToken());

            // 인증 실패와 관련된 정보를 세션에서 제거. 즉 다음에 재로그인할때 만약 이전 인증 실패 정보가 남아있다면 이전 인증 실패 정보가
            // 남아있지않도록 함.
            callSuperClearAuthenticationAttributes(request);

            return ResponseEntity.ok().headers(headers)
                    .body(new SuccessResponse<>("로그인에 성공하였습니다."));
        } catch (AuthenticationException e) {
            log.info("[AuthController] login() 요청 실패: 아이디 및 패스워드가 틀림(로그인 실패) 분기 응답", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("아이디 및 패스워드를 틀렸습니다.", HttpStatus.UNAUTHORIZED.value()));
        }
    }

    // Custom logout 로직을 구현한 경우 시큐리티에서 제공하는 logout을 사용하지 않는다.
    // 토큰이 변조되지만 않고 시간상 만료되어도 로그아웃 처리하기 위해 jwt 필터 사용하지 않고 여기서 처리
    @Operation(summary = "로그아웃 요청 처리", description = "사용자가 로그아웃 요청 진행")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유효한 토큰 또는 만료된 토큰으로 로그아웃 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "401", description = "변조된 토큰으로 인한 로그아웃 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

    })
    @PostMapping("/logout")
    @Transactional // deleteRefreshToken DB작업 있어서 트랜잭션 추가해야함
    public ResponseEntity<BaseResponse> logout(HttpServletRequest request, HttpServletResponse response) {

        log.info("[AuthController] logout() 요청");

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

            log.info("[AuthController] logout() 요청 실패: authorizationHeader가 존재하지 않는 경우 분기 응답");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("토큰 헤더가 비어있거나 Bearer 로 시작하지 않습니다.", HttpStatus.UNAUTHORIZED.value()));
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

            log.info("[AuthController] logout() 요청 성공 - 만료되지 않은 토큰으로 로그아웃 성공");

            return ResponseEntity.ok().body(new SuccessResponse<>("로그아웃에 성공하였습니다."));

        } catch (ExpiredJwtException e) {

            log.error("[AuthController] logout() 메서드 - ExpiredJwtException 에러 발생: ", e);

            // 만료된 토큰일 때도 userId를 추출 가능 (ExpiredJwtException을 통해 Claims에 접근) 즉
            // ExpiredJwtException을 통해 만료된 토큰에 있는 Claims에 접근한다.
            logoutProcessService.logoutUserByExpiredToken(e);

            log.info("[AuthController] logout() 요청 성공 - 만료된 토큰으로 로그아웃 성공");

            return ResponseEntity.ok().body(new SuccessResponse<>("로그아웃에 성공하였습니다."));

        } catch (Exception e) {
            log.error("[AuthController] logout() 메서드 - Exception 에러 발생: ", e);
            // 유효하지 않은 토큰(서명이 잘못되거나 변조된 경우 등 즉 비정상적인 토큰일 경우)이면 거부 한다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("토큰이 유효하지 않습니다. 재 로그인 해주세요.", HttpStatus.UNAUTHORIZED.value()));
        }

    }

    @Operation(summary = "특정 사용자의 작성자 여부 확인", description = "특정 사용자의 작성자 여부 요청을 처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "작성자 여부 응답 성공(True Or False)", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),

    })
    @Parameter(name = "blogId", description = "사용자 블로그 아이디", required = true)
    @PreAuthorize("isAuthenticated()") // 없어도 tokenAuthenticationFilter에 의해 검증되긴 하지만, 가독성을 위해 추가
    @GetMapping("/{blogId}/verify-author")
    public ResponseEntity<BaseResponse> verifyAuthor(
            // @AuthenticationPrincipal org.springframework.security.core.userdetails.User
            // user,
            @AuthenticationPrincipal BlogUser blogUser,
            @PathVariable("blogId") String blogId) {

        log.info("[PostFindController] verifyAuthor() 요청");

        String blogIdFromToken = blogUser.getBlogIdFromToken();

        // 로그인한 사용자와 실제 게시글 작성자가 같은지 최종적으로 확인. 아래 두 방식 둘다 가능 근데 내가 확장시킨 BlogUser사용
        // boolean isAuthor = blogId.equals(user.getUsername());
        boolean isAuthor = blogId.equals(blogIdFromToken);

        return ResponseEntity.ok().body(new SuccessResponse<>(isAuthor, "작성자 여부 응답에 성공하였습니다."));
    }

    // 테스트 코드에서 검증을 위해 추가
    protected void callSuperClearAuthenticationAttributes(HttpServletRequest request) {
        super.clearAuthenticationAttributes(request);

    }

}
