package com.yhs.blog.springboot.jpa.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yhs.blog.springboot.jpa.common.response.BaseResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.UserSettingsRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserPrivateProfileResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserPublicProfileResponse;
import com.yhs.blog.springboot.jpa.domain.user.service.UserProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Tag(name = "사용자 프로필", description = "사용자 프로필 조회 및 사용자 프로필 변경 API")
@Log4j2
@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /* formData를 객체에 바인딩하기 위해서 RequestBody 대신 ModelAttribute 사용 한다 */
    @Operation(summary = "사용자 설정 업데이트", description = "사용자 프로필 설정 업데이트 진행")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 프로필 설정 업데이트 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "blogId를 통해 사용자를 조회할 수 없는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한이 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameter(name = "blogId", description = "블로그 ID", required = true)
    @PatchMapping("/{blogId}/settings")
    @PreAuthorize("#userBlogId == authentication.name")
    public ResponseEntity<BaseResponse> updateSettings(
            @P("userBlogId") @PathVariable("blogId") String blogId,
            @ModelAttribute @Valid UserSettingsRequest settingsRequest) {

        log.info("[UserProfileController] updateSettings() 요청");

        try {
            userProfileService.updateUserSettings(blogId, settingsRequest);

            return ResponseEntity.ok()
                    .body(new SuccessResponse<>("사용자 설정이 성공적으로 업데이트되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                    "사용자 프로필 업데이트 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }

    }

    @Operation(summary = "사용자 민감하지 않은 정보 조회", description = "블로그Id, 블로그명, 사용자명, 프로필 이미지 url, 소개글을 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 민감하지 않은 프로필 정보 조회 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "blogId를 통해 사용자를 조회할 수 없는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping("/{blogId}/profile")
    @Parameter(name = "blogId", description = "블로그 ID", required = true)
    public ResponseEntity<BaseResponse> getUserProfilePublic(@PathVariable("blogId") String blogId) {

        log.info("[UserProfileController] getUserProfilePublic() 요청 - blogId: {}", blogId);

        UserPublicProfileResponse publicUserProfile = userProfileService.getUserPublicProfile(blogId);

        return ResponseEntity.ok().body(new SuccessResponse<>(publicUserProfile, "공개 사용자 정보 조회를 성공하였습니다."));
    }

    @Operation(summary = "사용자 이메일이 포함된 정보 조회 ", description = "이메일, 블로그Id, 블로그명, 사용자명, 프로필 이미지 url 조회 - 사용자가 로그인 후 개인 계정에 이메일을 표시할 수 있도록 하기 위함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 이메일이 포함된 정보 조회 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "blogId를 통해 사용자를 조회할 수 없는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    // 토큰 필터에서 인증 검사 끝나서 여기서 isAuthenticaed()필요x
    @GetMapping("/profile/private")
    public ResponseEntity<BaseResponse> getUserProfilePrivate(HttpServletRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

        log.info("[UserProfileController] getUserProfilePrivate() 요청");

        String blogId = user.getUsername();

        UserPrivateProfileResponse privateUserProfile = userProfileService.getUserPrivateProfile(blogId);

        return ResponseEntity.ok().body(new SuccessResponse<>(privateUserProfile, "비공개 사용자 정보 조회를 성공하였습니다."));
    }

}
