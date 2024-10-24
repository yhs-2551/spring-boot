package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.*;
import com.yhs.blog.springboot.jpa.entity.Post;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping
    public ResponseEntity<Object> createPost(@Valid @RequestBody PostRequest postRequest, HttpServletRequest request) {

        // TokenAuthenticationFilter를 security에 등록해두었기 때문에, TokenAuthenticationFilter의
        // SecurityContextHolder.getContext().setAuthentication(authentication)로 저장해둔 인증 정보를 가져옴
        // 근데 TokenAuthenticationFilter에서 유효성 검사 통과하지 못하면 return 으로 모두 처리해두었기 때문에 불필요한 것 같아 주석 처리
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized User");
//        }
        PostResponse responseDTO = postService.createPost(postRequest, request);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> findAllPosts() {
        List<PostResponse> postResponses = postService.getPostList();
        return ResponseEntity.ok().body(postResponses);
//        return new ResponseEntity<>(postResponses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> findPostById(@PathVariable("id") Long postId) {

        PostResponse postResponse = postService.getPost(postId);
        return ResponseEntity.ok().body(postResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteArticle(@PathVariable("id") Long id, HttpServletRequest request) {

//        TokenAuthenticationFilter에서 유효성 검사 실패하면 return 으로 모두 처리해두었기 때문에 불필요
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized User");
//        }

        Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
        PostResponse postResponseDTO = postService.getPost(id);

        if (!postResponseDTO.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this post.");
        }

        postService.deletePost(id);
        return ResponseEntity.ok("File deleted successfully");
    }

    //여기부터 작성
    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateArticle(@PathVariable Long id, @RequestBody PostUpdateRequest postUpdateRequest, HttpServletRequest request) {

        Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
        PostResponse postResponseDTO = postService.getPost(id);

        if (!postResponseDTO.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this post.");
        }

        Post updatedPost = postService.updatePost(id, postUpdateRequest);
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

//    @PostMapping("/files/delete-temp-files")
//    public ResponseEntity<String> deleteTempFiles(@RequestBody Map<String, List<String>> fileUrls) {
//        log.info("fileUrls: " + fileUrls);
//        try {
//            List<String> urls = fileUrls.get("urls");
//            log.info("urls: " + urls);
//            for (String fileUrl : urls) {
//                log.info("실행");
//                s3Service.tempDeleteFile(fileUrl, null);
//            }
//            return ResponseEntity.ok("Files deleted successfully");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete files");
//        }
//
//    }
//
//    @PostMapping("/file/delete-temp-featured-file")
//    public ResponseEntity<String> deleteFeaturedFile(@RequestBody Map<String,
//            String > payload) {
//
//        try {
//            String url = payload.get("url");
//            String featuredString =  payload.get("featured");
//            log.info("urls: " + url);
//
//            s3Service.tempDeleteFile(url, featuredString);
//            return ResponseEntity.ok("Featured File deleted successfully");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete" + " featured file");
//        }
//
//    }


}


