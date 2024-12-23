package com.yhs.blog.springboot.jpa.domain.post.controller;

import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostUpdateResponse;
import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.post.service.PostService;
import com.yhs.blog.springboot.jpa.domain.file.service.s3.S3Service;
import com.yhs.blog.springboot.jpa.domain.token.jwt.util.TokenUtil;
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
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/{blogId}/posts")
@Log4j2
public class PostApiController {

        private final PostService postService;
        private final TokenProvider tokenProvider;
        private final S3Service s3Service;
        private final UserRepository userRepository;

        // ResponseEntity의 <?>와일드 카드 대신 sealed 클래스를 사용해 특정 클래스들만 상속하게 제한함
        @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        @PreAuthorize("#userBlogId == authentication.name")
        public ResponseEntity<ApiResponse> createNewPost(@P("userBlogId") @PathVariable("blogId") String blogId,
                        @Valid @RequestBody PostRequest postRequest,
                        HttpServletRequest request) {

                PostResponse responseDTO = postService.createNewPost(postRequest, request);
                // 아래 응답에서 일단 responseDTO를 사용하고 있지만, 나중에는 그냥 문자열로만 응답하도록 수정할수도 있음.
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new SuccessResponse<PostResponse>(responseDTO, "Success create new post."));
        }

        @GetMapping
        public ResponseEntity<ApiResponse> findAllPosts(@PathVariable("blogId") String blogId,
                        @RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "searchType", required = false) SearchType searchType,
                        @RequestParam(name = "categoryUuid", required = false) String categoryUuid,
                        @PageableDefault(page = 0, size = 10, sort = { "createdAt",
                                        "id" }, direction = Sort.Direction.DESC) Pageable pageable) {

                // 게시글 전체는 특정 사용자 즉 정확한 해당 사용자의 게시글만 조회 가능하도록 구현(userIdentifier 사용)
                User user = userRepository.findByBlogId(blogId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with user identifier " + blogId));
                Long userId = user.getId();
                // List<PostResponse> postResponses = postService.getPostListByUserId(userId);
                Page<PostResponse> postResponses = postService.getPosts(userId, keyword, searchType, categoryUuid,
                                pageable);

                return ResponseEntity.ok(new SuccessResponse<>(postResponses, "게시글 응답에 성공하였습니다."));
                // return new ResponseEntity<>(postResponses, HttpStatus.OK);
        }

        @GetMapping("/{postId}")
        public ResponseEntity<PostResponse> findPostByPostId(@PathVariable("postId") Long postId) {

                PostResponse postResponse = postService.getPostByPostId(postId);
                return ResponseEntity.ok().body(postResponse);
        }

        @GetMapping("/{postId}/verify-author")
        public ResponseEntity<Map<String, Boolean>> verifyAuthor(HttpServletRequest request,
                        @PathVariable("postId") Long postId,
                        @PathVariable("blogId") String blogId) {

                // url 경로에서 받은 userIdentifier을 통해 정확히 해당 사용자의 게시글을 가져오기 위함
                Long userId = userRepository.findByBlogId(blogId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with user identifier " + blogId))
                                .getId();

                // 로그인한 사용자의 userId
                Long userIdFromAccessToken = TokenUtil.extractUserIdFromRequestToken(request,
                                tokenProvider);

                // 로그인한 사용자와 실제 게시글 작성자가 같은지 최종적으로 확인
                boolean isAuthor = userId.equals(userIdFromAccessToken);

                Map<String, Boolean> response = new HashMap<>();
                response.put("isAuthor", isAuthor);
                return ResponseEntity.ok(response);
        }

        @PreAuthorize("#userBlogId == authentication.name")
        @DeleteMapping("/{postId}")
        public ResponseEntity<ApiResponse> deletePostById(@PathVariable("postId") Long postId,
                        @P("userBlogId") @PathVariable("blogId") String blogId) {

                postService.deletePostByPostId(postId);
                return ResponseEntity.ok(new SuccessResponse<>("게시글이 성공적으로 삭제되었습니다."));
        }

        @PreAuthorize("#userBlogId == authentication.name")
        @PatchMapping("/{postId}")
        public ResponseEntity<ApiResponse> updatePostByPostId(@PathVariable("postId") Long postId,
                        @P("userBlogId") @PathVariable("blogId") String blogId,
                        @RequestBody PostUpdateRequest postUpdateRequest) {

                Long userId = userRepository.findByBlogId(blogId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with user identifier " + blogId))
                                .getId();

                Post updatedPost = postService.updatePostByPostId(postId, userId,
                                postUpdateRequest);
                PostUpdateResponse postUpdateResponse = new PostUpdateResponse(updatedPost);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(new SuccessResponse<PostUpdateResponse>(postUpdateResponse,
                                                "Success update post" +
                                                                "."));
        }

        // Swagger api에서 파일을 선택하기 위해서 consumes = "multipart/form-data"를 지정해 주어야 함.
        @PreAuthorize("#userBlogId == authentication.name")
        @PostMapping(value = "/temp/files/upload", consumes = "multipart/form-data", produces = MediaType.TEXT_PLAIN_VALUE)
        public ResponseEntity<String> uploadFile(
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(value = "featured", required = false) String featured,
                        @P("userBlogId") @PathVariable("blogId") String blogId,
                        HttpServletRequest request) {

                try {

                        if (Objects.requireNonNull(file.getContentType()).startsWith("image/")
                                        && file.getSize() > 5 * 1024 * 1024) { // 5MB
                                // limit for image files
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body("Image file size exceeds the limit of 5MB");
                        } else if (!file.getContentType().startsWith("image/") && file.getSize() > 10 * 1024 * 1024) { // 10MB
                                                                                                                       // limit
                                                                                                                       // for
                                                                                                                       // other
                                                                                                                       // files
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body("File size exceeds the limit of 10MB");
                        }

                        String fileUrl = s3Service.tempUploadFile(file, featured, blogId);

                        return ResponseEntity.ok(fileUrl);
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
                }

        }

}
