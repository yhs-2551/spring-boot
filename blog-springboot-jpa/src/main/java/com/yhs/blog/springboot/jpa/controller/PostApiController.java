package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostResponse;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.dto.PostUpdateResponse;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.service.PostService;
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

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
@Log4j2
public class PostApiController {

    private final PostService postService;
    private final TokenProvider tokenProvider;

    @PostMapping
    public ResponseEntity<Object> createPost(@Valid @RequestBody PostRequest postRequest,
                                             HttpServletRequest request) {

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
    public ResponseEntity<String> deleteArticle(@PathVariable("id") Long id,
                                           HttpServletRequest request) {

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
        return ResponseEntity.ok().build();
    }

    //여기부터 작성
    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateArticle(@PathVariable Long id,
                                           @RequestBody PostUpdateRequest postUpdateRequest,
                                                HttpServletRequest request) {

        Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
        PostResponse postResponseDTO = postService.getPost(id);

        if (!postResponseDTO.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this post.");
        }

        Post updatedPost = postService.updatePost(id, postUpdateRequest);
        PostUpdateResponse postUpdateResponse = new PostUpdateResponse(updatedPost);
        return ResponseEntity.ok().body(postUpdateResponse);
    }


}


