package com.yhs.blog.springboot.jpa.domain.post.controller;

import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PageResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostUpdateResponse;
import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.post.service.PostService;
import com.yhs.blog.springboot.jpa.domain.category.repository.CategoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Log4j2
public class PostController {

        private final PostService postService;

        // ResponseEntity의 <?>와일드 카드 대신 sealed 클래스를 사용해 특정 클래스들만 상속하게 제한함
        @PostMapping(value = "/{blogId}/posts", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @PreAuthorize("#userBlogId == authentication.name")
        public ResponseEntity<ApiResponse> createNewPost(@P("userBlogId") @PathVariable("blogId") String blogId,
                        @Valid @RequestBody PostRequest postRequest,
                        HttpServletRequest request) {

                PostResponse responseDTO = postService.createNewPost(postRequest, request);
                // 아래 응답에서 일단 responseDTO를 사용하고 있지만, 나중에는 그냥 문자열로만 응답하도록 수정할수도 있음.
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new SuccessResponse<PostResponse>(responseDTO, "Success create new post."));
        }

        @GetMapping({ "/{blogId}/posts", "/posts" })
        public ResponseEntity<ApiResponse> findAllPosts(@PathVariable(name = "blogId", required = false) String blogId,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "searchType", required = false) SearchType searchType,
                        @RequestParam(name = "category", required = false) String category, // 검색할때는 카테고리를 쿼리 파라미터에 포함
                        @PageableDefault(page = 0, size = 10, sort = { "createdAt",
                                        "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

                Page<PostResponse> postResponses;

                if (blogId != null) {

                        // 특정 사용자의 전체 게시글 처리
                        // 특정 사용자 즉 정확한 해당 사용자의 게시글만 조회 가능하도록 구현(blogId 사용)
                        if (category != null) {

                                postResponses = postService.getAllPostsSpecificUser(blogId, keyword, searchType,
                                                category,
                                                pageable);
                        } else {

                                postResponses = postService.getAllPostsSpecificUser(blogId, keyword, searchType, null,
                                                pageable);
                        }

                } else {
                        // 모든 사용자의 전체 게시글 조회
                        postResponses = postService.getAllPostsAllUser(keyword, searchType, pageable);

                }

                PageResponse<PostResponse> pageResponse = new PageResponse<>(postResponses);

                return ResponseEntity.ok(new SuccessResponse<>(pageResponse, "게시글 응답에 성공하였습니다."));
        }

        // 경로 변수는 CustomPageableResolver가 작동이 안해서 int pageNumber = page - 1;로 처리. 쿼리
        // 파라미터에만 작동이 되는건가?
        @GetMapping("/{blogId}/posts/page/{page}")
        public ResponseEntity<ApiResponse> getPostsByPage(
                        @PathVariable("blogId") String blogId,
                        @PathVariable("page") Integer page, // String 값으로 넘어오게 되는데, Spring의 타입 컨버터가 자동으로 String →
                                                            // Integer 변환
                        @PageableDefault(page = 0, size = 10, sort = { "createdAt",
                                        "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

                int pageNumber = page - 1;

                PageRequest pageRequest = PageRequest.of(
                                pageNumber,
                                pageable.getPageSize(),
                                pageable.getSort());

                Page<PostResponse> postResponses = postService.getAllPostsSpecificUser(blogId, null, null,
                                null,
                                pageRequest);

                PageResponse<PostResponse> pageResponse = new PageResponse<>(postResponses);

                return ResponseEntity.ok(new SuccessResponse<>(pageResponse, page + "번 페이지 게시글 응답에 성공하였습니다."));
        }

        @GetMapping("/{blogId}/categories/{category}/posts")
        public ResponseEntity<ApiResponse> getPostsByCategoryAndUser(
                        @PathVariable("blogId") String blogId,
                        @PathVariable("category") String category,
                        @PageableDefault(page = 0, size = 10, sort = { "createdAt",
                                        "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

                Page<PostResponse> postResponses = postService.getAllPostsSpecificUser(blogId, null, null, category,
                                pageable);

                PageResponse<PostResponse> pageResponse = new PageResponse<>(postResponses);

                return ResponseEntity.ok(new SuccessResponse<>(pageResponse, "특정 카테고리별 게시글 응답에 성공하였습니다."));
        }

        @GetMapping("/{blogId}/categories/{category}/posts/page/{page}")
        public ResponseEntity<ApiResponse> getPostsByCategoryAndPage(
                        @PathVariable("blogId") String blogId,
                        @PathVariable("category") String category,
                        @PathVariable("page") Integer page,
                        @PageableDefault(page = 0, size = 10, sort = { "createdAt",
                                        "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

                int pageNumber = page - 1;

                PageRequest pageRequest = PageRequest.of(
                                pageNumber,
                                pageable.getPageSize(),
                                pageable.getSort());

                Page<PostResponse> postResponses = postService.getAllPostsSpecificUser(blogId, null, null,
                                category,
                                pageRequest);

                PageResponse<PostResponse> pageResponse = new PageResponse<>(postResponses);

                return ResponseEntity.ok(new SuccessResponse<>(pageResponse,
                                String.format("%s 카테고리 %d번 페이지 게시글 응답에 성공하였습니다.", category, page)));
        }

        @GetMapping("/{blogId}/posts/{postId}")
        public ResponseEntity<PostResponse> findPostByPostId(@PathVariable("postId") Long postId) {

                PostResponse postResponse = postService.getPostByPostId(postId);
                return ResponseEntity.ok().body(postResponse);
        }

        @PreAuthorize("isAuthenticated()") // 없어도 tokenAuthenticationFilter에 의해 검증되긴 하지만, 가독성을 위해 추가
        @GetMapping("/{blogId}/posts/{postId}/verify-author")
        public ResponseEntity<Map<String, Boolean>> verifyAuthor(
                        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
                        @PathVariable("blogId") String blogId) {

                // 로그인한 사용자와 실제 게시글 작성자가 같은지 최종적으로 확인
                boolean isAuthor = blogId.equals(user.getUsername());

                Map<String, Boolean> response = new HashMap<>();
                response.put("isAuthor", isAuthor);
                return ResponseEntity.ok(response);
        }

        @PreAuthorize("#userBlogId == authentication.name")
        @DeleteMapping("/{blogId}/posts/{postId}")
        public ResponseEntity<ApiResponse> deletePostById(@PathVariable("postId") Long postId,
                        @P("userBlogId") @PathVariable("blogId") String blogId) {

                postService.deletePostByPostId(postId);
                return ResponseEntity.ok(new SuccessResponse<>("게시글이 성공적으로 삭제되었습니다."));
        }

        @PreAuthorize("#userBlogId == authentication.name")
        @PatchMapping("/{blogId}/posts/{postId}")
        public ResponseEntity<ApiResponse> updatePostByPostId(@PathVariable("postId") Long postId,
                        @P("userBlogId") @PathVariable("blogId") String blogId,
                        @RequestBody PostUpdateRequest postUpdateRequest) {

                PostUpdateResponse postUpdateResponse = postService.updatePostByPostId(postId, blogId,
                                postUpdateRequest);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(new SuccessResponse<PostUpdateResponse>(postUpdateResponse,
                                                "Success update post" +
                                                                "."));
        }

}
