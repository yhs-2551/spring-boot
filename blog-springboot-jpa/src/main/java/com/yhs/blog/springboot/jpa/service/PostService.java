package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostResponse;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.dto.PostUpdateResponse;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.entity.User;

import java.security.Principal;
import java.util.List;

public interface PostService {

    PostResponse createPost(PostRequest postRequest, Principal principal);

    List<Post> getList();

    Post getPost(Long id);

    void deletePost(Long id);

    Post updatePost(Long id, PostUpdateRequest postUpdateRequest);

}
