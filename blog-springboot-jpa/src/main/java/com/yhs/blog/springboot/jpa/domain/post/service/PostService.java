package com.yhs.blog.springboot.jpa.domain.post.service;

import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostUpdateResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostResponse createNewPost(PostRequest postRequest, HttpServletRequest request);

    // List<PostResponse> getPostListByUserId(Long UserId);
    // Page<PostResponse> getAllPostsSpecificUser(Long userId, String keyword, SearchType searchType, String categoryId, Pageable pageable);
    Page<PostResponse> getAllPostsSpecificUser(String blogId, String keyword, SearchType searchType, String categoryName, Pageable pageable);
    
    Page<PostResponse> getAllPostsAllUser(String keyword, SearchType searchType, Pageable pageable);

    PostResponse getPostByPostId(Long postId);

    void deletePostByPostId(Long postId);

    PostUpdateResponse updatePostByPostId(Long postI, String blogId,
                            PostUpdateRequest postUpdateRequest);

}
