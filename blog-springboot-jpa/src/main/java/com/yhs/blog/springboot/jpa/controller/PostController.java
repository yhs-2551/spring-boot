package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.dto.PostDTO;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.service.PostService;
import com.yhs.blog.springboot.jpa.util.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

     private final PostService postService;


    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO) {
        Post post = postService.createPost(postDTO);
        PostDTO responseDTO = PostMapper.toDTO(post);

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

}
