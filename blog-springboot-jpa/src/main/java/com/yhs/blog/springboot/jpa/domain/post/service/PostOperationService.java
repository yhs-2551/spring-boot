package com.yhs.blog.springboot.jpa.domain.post.service;

import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;

public interface PostOperationService {

    void createNewPost(PostRequest postRequest, BlogUser blogUser);

    void deletePostByPostId(Long postId, BlogUser blogUser);

    void updatePostByPostId(Long postI, BlogUser blogUser,
            PostUpdateRequest postUpdateRequest);
}
