package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostResponse;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.dto.PostUpdateResponse;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.service.PostService;
import com.yhs.blog.springboot.jpa.util.PostMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostRequest> createPost(@Valid @RequestBody PostRequest postRequest) {
        Post post = postService.createPost(postRequest);
        PostRequest responseDTO = PostMapper.toDTO(post);

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> findAllPosts() {
        List<PostResponse> postResponses = postService.getList().stream().map(post -> new PostResponse(post)).toList();
        return ResponseEntity.ok().body(postResponses);
//        return new ResponseEntity<>(postResponses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> findPostById(@PathVariable("id") Long id) {
        Post post = postService.getPost(id);
        return ResponseEntity.ok().body(new PostResponse(post));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    //여기부터 작성
    @PatchMapping("{id}")
    public ResponseEntity<PostUpdateResponse> updateArticle(@PathVariable Long id, @RequestBody PostUpdateRequest postUpdateRequest) {
        Post updatedPost = postService.updatePost(id, postUpdateRequest);
        PostUpdateResponse postUpdateResponse = new PostUpdateResponse(updatedPost);
        return ResponseEntity.ok().body(postUpdateResponse);
    }


}


