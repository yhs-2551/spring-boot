package com.yhs.blog.springboot.jpa.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yhs.blog.springboot.jpa.aop.performance.MeasurePerformance;
import com.yhs.blog.springboot.jpa.common.response.BaseResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.service.UserCheckService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Tag(name = "사용자 검증", description = "회원가입 시 블로그ID/사용자명(닉네임)/이메일 중복 검사 및 사용자 존재 여부 확인, 사용자 정보 검증 API")
@Log4j2
@RequestMapping("/api/check")
@RestController
@RequiredArgsConstructor
public class UserCheckController {

    private final UserCheckService userCheckService;

    // 상대적으로 자주 조회되며 변경이 적은 데이터, redis 적용으로 실행 시간 10ms 이하로 성능 향상, 기존 100ms 이상, DB 부하 감소
    @MeasurePerformance
    @Operation(summary = "blogId를 통해 사용자 존재 여부 파악 ", description = "blogId를 통해 사용자 존재 여부 파악")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자가 존재하는 경우 ", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자가 존재하지 않는 경우 ", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @Parameter(name = "blogId", description = "확인할 블로그 ID", required = true)
    @GetMapping("/blog-id/exists/{blogId}")
    public ResponseEntity<BaseResponse> checkExistsBlogId(@PathVariable("blogId") String blogId) {

        log.info("[UserCheckController] checkExistsBlogId() 요청 - blogId: {}", blogId);

        if (userCheckService.isExistsBlogId(blogId)) {
            log.info("[UserCheckController] checkExistsBlogId() 요청 성공 - blogId 존재 분기 응답");

            return ResponseEntity.ok()
                    .body(new SuccessResponse<>(blogId + " 사용자가 존재 합니다."));
        }

        log.info("[UserCheckController] checkExistsBlogId() 요청 실패 - blogId 미존재 분기 응답");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(blogId + " 사용자를 조회할 수 없습니다.", 404));
    }

    @Operation(summary = "회원가입시 blogId 중복 확인 ", description = "회원가입시 blogId 중복 확인 진행")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "429", description = "너무 많은 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하고 있는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameter(name = "blogId", description = "확인할 블로그 ID", required = true)
    @GetMapping("/blog-id/duplicate/{blogId}")
    public ResponseEntity<BaseResponse> checkDuplicateBlogId(@PathVariable("blogId") String blogId) {

        log.info("[UserCheckController] checkDuplicateBlogId() 요청 - blogId: {}", blogId);

        DuplicateCheckResponse response = userCheckService.isDuplicateBlogId(blogId);

        return checkDuplicate(response);
    }

    @Operation(summary = "회원가입시 이메일 중복 확인 ", description = "회원가입시 이메일 중복 확인 진행")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "429", description = "너무 많은 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하고 있는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameter(name = "email", description = "확인할 이메일", required = true)
    @GetMapping("/email/duplicate/{email}")
    public ResponseEntity<BaseResponse> checkDuplicateEmail(@PathVariable("email") String email) {

        log.info("[UserCheckController] checkDuplicateEmail() 요청");

        DuplicateCheckResponse response = userCheckService.isDuplicateEmail(email);

        return checkDuplicate(response);

    }

    @Operation(summary = "회원가입시 username 중복 확인 ", description = "회원가입시 username 중복 확인 진행")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "429", description = "너무 많은 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하고 있는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameter(name = "username", description = "확인할 사용자명", required = true)
    @GetMapping("/username/duplicate/{username}")
    public ResponseEntity<BaseResponse> checkDuplicateUsername(@PathVariable("username") String username) {

        log.info("[UserCheckController] checkDuplicateUsername() 요청 - username: {}", username);

        DuplicateCheckResponse response = userCheckService.isDuplicateUsername(username);

        return checkDuplicate(response);
    }

    private ResponseEntity<BaseResponse> checkDuplicate(DuplicateCheckResponse response) {

        if (response.isExist()) {
            log.info("[UserCheckController] checkDuplicate() 중복확인 요청 실패 - 이미 존재하는 경우 분기 응답");

            // 이미 존재하는 경우
            return ResponseEntity.status(HttpStatus.CONFLICT) // 409
                    .body(new ErrorResponse(response.getMessage(), HttpStatus.CONFLICT.value()));
        }

        log.info("[UserCheckController] checkDuplicate() 중복확인 요청 성공 - 존재하지 않는 경우 분기 응답");

        return ResponseEntity.ok(new SuccessResponse<>(response.isExist(), response.getMessage()));
    }

}
