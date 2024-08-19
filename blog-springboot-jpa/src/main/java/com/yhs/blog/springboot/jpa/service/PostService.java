package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.dto.PostUpdateResponse;
import com.yhs.blog.springboot.jpa.entity.Post;

import java.util.List;

public interface PostService {

    Post createPost(PostRequest postRequest);

    List<Post> getList();

    Post getPost(Long id);

    void deletePost(Long id);

    Post updatePost(Long id, PostUpdateRequest postUpdateRequest);
}
