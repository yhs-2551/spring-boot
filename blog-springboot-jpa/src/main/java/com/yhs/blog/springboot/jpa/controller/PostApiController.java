package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.*;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.repository.UserRepository;
import com.yhs.blog.springboot.jpa.service.PostService;
import com.yhs.blog.springboot.jpa.service.S3Service;
import com.yhs.blog.springboot.jpa.util.PostMapper;
import com.yhs.blog.springboot.jpa.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
@Log4j2
public class PostApiController {

    private final PostService postService;
    private final TokenProvider tokenProvider;
    private final S3Service s3Service;
    private final UserRepository userRepository;

    //ResponseEntity의 <?>와일드 카드 대신 sealed 클래스를 사용해 특정 클래스들만 상속하게 제한함
    @PostMapping(
            value = "/{userIdentifier}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createNewPost(@PathVariable("userIdentifier") String userIdentifier,
                                                     @Valid @RequestBody PostRequest postRequest,
                                                     HttpServletRequest request) {
        log.info("실행입니다");
        
        String userIdentifierFromAccessToken =
                TokenUtil.extractUserIdentifierFromRequestToken(request,
                        tokenProvider);

        if (!userIdentifier.equals(userIdentifierFromAccessToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("You are not authorized to create this post.", 403));
        }

        PostResponse responseDTO = postService.createNewPost(postRequest, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<PostResponse>(responseDTO, "Success create new post."));
    }

    @GetMapping("/{userIdentifier}")
    public ResponseEntity<List<PostResponse>> findAllPosts(@PathVariable("userIdentifier") String userIdentifier, HttpServletRequest request) {

        // 게시글 전체는 특정 사용자 즉 정확한 해당 사용자의 게시글만 조회 가능하도록 구현(userIdentifier 사용)
        User user = userRepository.findByUserIdentifier(userIdentifier)
                .orElseThrow(() -> new RuntimeException("User not found with user identifier " + userIdentifier));
        Long userId = user.getId();

        List<PostResponse> postResponses = postService.getPostListByUserId(userId);
        return ResponseEntity.ok().body(postResponses);
//        return new ResponseEntity<>(postResponses, HttpStatus.OK);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> findPostByPostId(@PathVariable("postId") Long postId) {

        PostResponse postResponse = postService.getPostByPostId(postId);
        return ResponseEntity.ok().body(postResponse);
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity<String> deletePostById(@PathVariable("postId") Long postId,
                                                 HttpServletRequest request) {

        Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
        PostResponse postResponseDTO = postService.getPostByPostId(postId);

        if (!postResponseDTO.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this post.");
        }

        postService.deletePostByPostId(postId);
        return ResponseEntity.ok("File deleted successfully");
    }

    @PatchMapping("/post/{postId}")
    public ResponseEntity<Object> updatePostByPostId(@PathVariable("postId") Long postId,
                                                     @RequestBody PostUpdateRequest postUpdateRequest, HttpServletRequest request) {

        Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
        PostResponse postResponseDTO = postService.getPostByPostId(postId);

        if (!postResponseDTO.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this post.");
        }

        Post updatedPost = postService.updatePostByPostId(postId, userId, postUpdateRequest);
        PostUpdateResponse postUpdateResponse = new PostUpdateResponse(updatedPost);
        return ResponseEntity.ok().body(postUpdateResponse);
    }

    @PostMapping("temp/files/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "featured", required = false) String featured) {
        try {
            if (Objects.requireNonNull(file.getContentType()).startsWith("image/") && file.getSize() > 5 * 1024 * 1024) { // 5MB
                // limit for image files
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image file size exceeds the limit of 5MB");
            } else if (!file.getContentType().startsWith("image/") && file.getSize() > 10 * 1024 * 1024) { // 10MB limit for other files
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds the limit of 10MB");
            }

            String fileUrl = s3Service.tempUploadFile(file, featured);

            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }

    }


}


