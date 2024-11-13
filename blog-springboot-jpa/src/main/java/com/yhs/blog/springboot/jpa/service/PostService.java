package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostResponse;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.entity.Post;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.List;

public interface PostService {

    PostResponse createNewPost(PostRequest postRequest, HttpServletRequest request);

    List<PostResponse> getPostListByUserId(Long UserId);

    PostResponse getPostByPostId(Long postId);

    void deletePostByPostId(Long postId);

    Post updatePostByPostId(Long postI, Long userId,
                            PostUpdateRequest postUpdateRequest);

}
