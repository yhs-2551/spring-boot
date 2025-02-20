package com.yhs.blog.springboot.jpa.domain.post.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yhs.blog.springboot.jpa.aop.performance.MeasurePerformance;
import com.yhs.blog.springboot.jpa.common.response.BaseResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.service.PostOperationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Tag(name = "게시글 생성, 수정, 삭제", description = "게시글 생성, 수정, 삭제 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Log4j2
public class PostOperationController {

        private final PostOperationService postOperationService;

        // ResponseEntity의 <?>와일드 카드 대신 sealed 클래스를 사용해 특정 클래스들만 상속하게 제한함
        @Operation(summary = "게시글 생성 요청 처리", description = "사용자가 게시글을 생성 요청을 보내면 해당 요청을 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "게시글 생성 응답 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "403", description = "권한없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameter(name = "blogId", description = "사용자 블로그 아이디", required = true)
        @MeasurePerformance
        @PostMapping(value = "/{blogId}/posts", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @PreAuthorize("#userBlogId == authentication.name")
        public ResponseEntity<BaseResponse> createNewPost(@P("userBlogId") @PathVariable("blogId") String blogId,
                        @RequestBody PostRequest postRequest, // @Valid 추가 필요 (?)
                        @AuthenticationPrincipal BlogUser blogUser) {

                log.info("[PostOperationController] createNewPost() 요청");

                postOperationService.createNewPost(postRequest, blogUser);

                // 아래 응답에서 일단 responseDTO를 사용하고 있지만, 나중에는 그냥 문자열로만 응답하도록 수정할수도 있음.
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new SuccessResponse<>("게시글 생성에 성공 하였습니다."));
        }

        @Operation(summary = "특정 사용자의 단일 게시글 삭제 요청 처리", description = "특정 사용자의 단일 게시글 삭제 요청 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "특정 사용자의 단일 게시글 삭제 성공 응답", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "특정 사용자의 단일 게시글 조회 실패 응답", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameters({
                        @Parameter(name = "blogId", description = "사용자 블로그 ID", required = true),
                        @Parameter(name = "postId", description = "삭제할 단일 게시글 ID", required = true),

        })
        @PreAuthorize("#userBlogId == authentication.name")
        @DeleteMapping("/{blogId}/posts/{postId}")
        public ResponseEntity<BaseResponse> deletePostById(@PathVariable("postId") Long postId,
                        @P("userBlogId") @PathVariable("blogId") String blogId,
                        @AuthenticationPrincipal BlogUser blogUser) {

                log.info("[PostOperationController] deletePostById() 요청");

                postOperationService.deletePostByPostId(postId, blogUser);

                return ResponseEntity.ok(new SuccessResponse<>("게시글이 성공적으로 삭제되었습니다."));
        }

        @Operation(summary = "특정 사용자의 단일 게시글 업데이트 요청 처리", description = "특정 사용자의 단일 게시글 업데이트 요청 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "특정 사용자의 단일 게시글 업데이트 성공 응답", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "특정 사용자의 단일 게시글 조회 실패 응답", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameters({
                        @Parameter(name = "blogId", description = "사용자 블로그 ID", required = true),
                        @Parameter(name = "postId", description = "수정할 단일 게시글 ID", required = true),

        })
        @MeasurePerformance
        @PreAuthorize("#userBlogId == authentication.name")
        @PatchMapping("/{blogId}/posts/{postId}")
        public ResponseEntity<BaseResponse> updatePostByPostId(@PathVariable("postId") Long postId,
                        @P("userBlogId") @PathVariable("blogId") String blogId,
                        @RequestBody PostUpdateRequest postUpdateRequest,
                        @AuthenticationPrincipal BlogUser blogUser) { // @Valid 추가 필요 (?)

                log.info("[PostOperationController] updatePostByPostId() 요청");

                postOperationService.updatePostByPostId(postId, blogUser,
                                postUpdateRequest);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(new SuccessResponse<>("게시글이 성공적으로 수정되었습니다."));
        }


        // @Operation(summary = "특정 사용자의 단일 게시글의 게시글 상태 업데이트 요청 처리", description = "특정 사용자의 단일 게시글에 대한 게시글 상태 업데이트 요청 처리")
        // @ApiResponses(value = {
        //                 @ApiResponse(responseCode = "200", description = "특정 사용자의 단일 게시글에 대한 게시글 상태 업데이트 성공 응답", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
        //                 @ApiResponse(responseCode = "401", description = "인증 실패(액세스 토큰 만료)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        //                 @ApiResponse(responseCode = "403", description = "권한 없음(게시글 작성자 본인이 아닌 경우)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        //                 @ApiResponse(responseCode = "404", description = "특정 사용자의 단일 게시글 조회 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        // })
        // @Parameters({
        //                 @Parameter(name = "blogId", description = "사용자 블로그 ID", required = true),
        //                 @Parameter(name = "postId", description = "수정할 단일 게시글 ID", required = true),

        // })
        // @PatchMapping("/{blogId}/posts/{postId}/{postStatus}")
        // @PreAuthorize("#userBlogId == authentication.name")
        // public ResponseEntity<BaseResponse> updatePostStatusByPostId(@PathVariable("postId") Long postId,
        //                 @P("userBlogId") @PathVariable("blogId") String blogId,
        //                 @PathVariable("postStatus") PostStatus status) { // 프론트 문자열 값을 enum으로 자동 변환해서 받을 수 있음. 단 정확히
        //                                                                  // 일치해야함

        //         log.info("[PostOperationController] updatePostStatusByPostId() 요청");

        //         postOperationService.updatePostStatusByPostId(postId, status);

        //         return ResponseEntity.status(HttpStatus.OK)
        //                         .body(new SuccessResponse<>("게시글 상태가 성공적으로 수정되었습니다."));
        // }

}
