package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostResponse;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.entity.Post;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.List;

public interface PostService {

    PostResponse createPost(PostRequest postRequest, HttpServletRequest request);

    List<PostResponse> getPostList();

    PostResponse getPost(Long id);

    void deletePost(Long id);

    Post updatePost(Long id, PostUpdateRequest postUpdateRequest);

}
