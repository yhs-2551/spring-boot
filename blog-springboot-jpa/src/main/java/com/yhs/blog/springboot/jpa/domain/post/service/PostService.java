package com.yhs.blog.springboot.jpa.domain.post.service;

import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface PostService {

    PostResponse createNewPost(PostRequest postRequest, HttpServletRequest request);

    List<PostResponse> getPostListByUserId(Long UserId);

    PostResponse getPostByPostId(Long postId);

    void deletePostByPostId(Long postId);

    Post updatePostByPostId(Long postI, Long userId,
                            PostUpdateRequest postUpdateRequest);

}
