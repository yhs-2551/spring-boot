package com.yhs.blog.springboot.jpa.domain.post.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yhs.blog.springboot.jpa.aop.performance.MeasurePerformance;
import com.yhs.blog.springboot.jpa.common.response.BaseResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PageResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.post.service.PostFindService;

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

@Tag(name = "게시글 조회", description = "모든 사용자의 게시글, 특정 사용자의 게시글 등 조회 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Log4j2
public class PostFindController {

        private final PostFindService postFindService;

        // 응답 DTO 필요한 데이터만 남겨서 1000건 데이터 기준 평균 4000ms -> 1800ms로 성능 개선
        // Projections.constructor을 사용하여 Entity전체가 아닌 필요한 컬럼만 조회, 연관 엔티티 추가 조회 없음(N+1문제
        // 해결), 조인으로 한 번에 데이터 조회로 성능 개선
        // 이에 따라 1800ms -> 600ms로 성능 개선
        // featuredImage 응답 DTO구조 featuredImage의 fileUrl만 리턴할 수 있도록 하면 좋음
        @Operation(summary = "모든 사용자의 게시글 조회 요청 처리", description = "사용자가 모든 사용자의 게시글을 조회 요청을 보내면 해당 요청을 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "모든 사용자 게시글 조회 응답 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameters({
                        @Parameter(name = "blogId", description = "사용자 블로그 ID", required = true),
                        @Parameter(name = "keyword", description = "검색 키워드", required = false),
                        @Parameter(name = "searchType", description = "검색 유형 TITLE, CONTENT, ALL", required = false),
                        @Parameter(name = "category", description = "특정 카테고리 조회", required = false),
        })
        @MeasurePerformance
        @GetMapping({ "/{blogId}/posts", "/posts" })
        public ResponseEntity<BaseResponse> findAllPosts(@PathVariable(name = "blogId", required = false) String blogId,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "searchType", required = false) SearchType searchType,
                        @RequestParam(name = "category", required = false) String category, // 검색할때는 카테고리를 쿼리 파라미터에 포함
                        @PageableDefault(page = 0, size = 10, sort = { "createdAt",
                                        "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

                log.info("[PostFindController] findAllPosts() 요청");

                Page<PostResponse> postResponses;

                if (blogId != null) {

                        // 특정 사용자의 전체 게시글 처리
                        // 특정 사용자 즉 정확한 해당 사용자의 게시글만 조회 가능하도록 구현(blogId 사용)
                        if (category != null) {

                                postResponses = postFindService.getAllPostsSpecificUser(blogId, keyword, searchType,
                                                category,
                                                pageable);
                        } else {

                                postResponses = postFindService.getAllPostsSpecificUser(blogId, keyword, searchType,
                                                null,
                                                pageable);
                        }

                } else {
                        // 모든 사용자의 전체 게시글 조회
                        postResponses = postFindService.getAllPostsAllUser(keyword, searchType, pageable);

                }

                PageResponse<PostResponse> pageResponse = new PageResponse<>(postResponses);

                return ResponseEntity.ok(new SuccessResponse<>(pageResponse, "게시글 응답에 성공하였습니다."));
        }

        // 경로 변수는 CustomPageableResolver가 작동이 안해서 int pageNumber = page - 1;로 처리.
        // CustomPageableResolver는 쿼리파라미터에만 작동. 이유는 MVC를 위해서 나왔기 때문
        @Operation(summary = "특정 사용자의 게시글 조회 요청 처리", description = "사용자가 특정 사용자의 게시글을 조회 요청을 보내면 해당 요청을 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "특정 사용자 게시글 조회 응답 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameters({
                        @Parameter(name = "blogId", description = "사용자 블로그 ID", required = true),
                        @Parameter(name = "page", description = "페이지 번호", required = true),
        })
        @GetMapping("/{blogId}/posts/page/{page}")
        public ResponseEntity<BaseResponse> getPostsByPage(
                        @PathVariable("blogId") String blogId,
                        @PathVariable("page") Integer page, // String 값으로 넘어오게 되는데, Spring의 타입 컨버터가 자동으로 String →
                                                            // Integer 변환
                        @PageableDefault(page = 0, size = 10, sort = { "createdAt",
                                        "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

                log.info("[PostFindController] getPostsByPage() 요청");

                int pageNumber = page - 1;

                PageRequest pageRequest = PageRequest.of(
                                pageNumber,
                                pageable.getPageSize(),
                                pageable.getSort());

                Page<PostResponse> postResponses = postFindService.getAllPostsSpecificUser(blogId, null, null,
                                null,
                                pageRequest);

                PageResponse<PostResponse> pageResponse = new PageResponse<>(postResponses);

                return ResponseEntity.ok(new SuccessResponse<>(pageResponse, page + "번 페이지 게시글 응답에 성공하였습니다."));
        }

        @Operation(summary = "특정 사용자의 카테고리 게시글 조회 요청 처리", description = "사용자가 특정 사용자의 카테고리 게시글 조회 요청을 보내면 해당 요청을 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "특정 사용자의 카테고리 게시글 응답 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameters({
                        @Parameter(name = "blogId", description = "사용자 블로그 ID", required = true),
                        @Parameter(name = "category", description = "특정 카테고리 조회", required = true),
        })
        @MeasurePerformance
        @GetMapping("/{blogId}/categories/{category}/posts")
        public ResponseEntity<BaseResponse> getPostsByCategoryAndUser(
                        @PathVariable("blogId") String blogId,
                        @PathVariable("category") String category,
                        @PageableDefault(page = 0, size = 10, sort = { "createdAt",
                                        "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

                log.info("[PostFindController] getPostsByCategoryAndUser() 요청");

                Page<PostResponse> postResponses = postFindService.getAllPostsSpecificUser(blogId, null, null, category,
                                pageable);

                PageResponse<PostResponse> pageResponse = new PageResponse<>(postResponses);

                return ResponseEntity.ok(new SuccessResponse<>(pageResponse, "특정 카테고리별 게시글 응답에 성공하였습니다."));
        }

        @Operation(summary = "특정 사용자의 카테고리 페이지별 게시글 조회 요청 처리", description = "사용자가 특정 사용자의 카테고리 페이지별 게시글 조회 요청을 보내면 해당 요청을 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "특정 사용자의 카테고리 페이지별 게시글 조회 응답 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameters({
                        @Parameter(name = "blogId", description = "사용자 블로그 ID", required = true),
                        @Parameter(name = "page", description = "페이지네이션 페이지 번호", required = true),
                        @Parameter(name = "category", description = "특정 카테고리 조회", required = true),

        })
        @GetMapping("/{blogId}/categories/{category}/posts/page/{page}")
        public ResponseEntity<BaseResponse> getPostsByCategoryAndPage(
                        @PathVariable("blogId") String blogId,
                        @PathVariable("category") String category,
                        @PathVariable("page") Integer page,
                        @PageableDefault(page = 0, size = 10, sort = { "createdAt",
                                        "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

                log.info("[PostFindController] getPostsByCategoryAndPage() 요청");

                int pageNumber = page - 1;

                PageRequest pageRequest = PageRequest.of(
                                pageNumber,
                                pageable.getPageSize(),
                                pageable.getSort());

                Page<PostResponse> postResponses = postFindService.getAllPostsSpecificUser(blogId, null, null,
                                category,
                                pageRequest);

                PageResponse<PostResponse> pageResponse = new PageResponse<>(postResponses);

                return ResponseEntity.ok(new SuccessResponse<>(pageResponse,
                                String.format("%s 카테고리 %d번 페이지 게시글 응답에 성공하였습니다.", category, page)));
        }

        @Operation(summary = "특정 사용자의 단일 게시글 조회 요청 처리", description = "사용자가 특정 사용자의 단일 게시글을 조회 요청을 보내면 해당 요청을 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "단일 게시글 응답 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "404", description = "단일 게시글 번호에 대해 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameter(name = "postId", description = "단일 게시글 번호", required = true)
        @GetMapping("/{blogId}/posts/{postId}")
        public ResponseEntity<BaseResponse> findPostByPostIdForDetailPage(@PathVariable("postId") Long postId) {

                log.info("[PostFindController] findPostByPostIdForDetailPage() 요청");

                PostResponse postResponse = postFindService.getPostByPostIdForDetailPage(postId);

                return ResponseEntity.ok().body(new SuccessResponse<>(postResponse, "상세 페이지 게시글 조회 응답에 성공하였습니다."));
        }

        @Operation(summary = "특정 사용자의 수정 페이지에 대한 단일 게시글 정보 조회 요청 처리", description = "사용자가 수정 페이지의 게시글 정보 조회 요청을 보내면 해당 요청을 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "단일 게시글 응답 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "404", description = "단일 게시글 번호에 대해 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameter(name = "postId", description = "단일 게시글 번호", required = true)
        @GetMapping("/{blogId}/posts/{postId}/edit")
        public ResponseEntity<BaseResponse> findPostByPostIdForEditPage(@PathVariable("postId") Long postId) {

                log.info("[PostFindController] findPostByPostIdForEditPage() 요청");

                PostResponse postResponse = postFindService.getPostByPostIdForEditPage(postId);

                return ResponseEntity.ok().body(new SuccessResponse<>(postResponse, "수정 페이지 게시글 조회 응답에 성공하였습니다."));
        }

}
