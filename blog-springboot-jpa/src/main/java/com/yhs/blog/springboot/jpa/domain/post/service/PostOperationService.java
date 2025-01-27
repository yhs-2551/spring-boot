package com.yhs.blog.springboot.jpa.domain.post.service;

import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;

public interface PostOperationService {

    void createNewPost(PostRequest postRequest, String blogId);

    void deletePostByPostId(Long postId, String blogId);

    void updatePostByPostId(Long postI, String blogId,
            PostUpdateRequest postUpdateRequest);
}
