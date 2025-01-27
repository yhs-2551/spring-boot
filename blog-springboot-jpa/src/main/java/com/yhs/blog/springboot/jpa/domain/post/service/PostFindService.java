package com.yhs.blog.springboot.jpa.domain.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;

public interface PostFindService {
    
    Page<PostResponse> getAllPostsSpecificUser(String blogId, String keyword, SearchType searchType,
            String categoryName, Pageable pageable);

    Page<PostResponse> getAllPostsAllUser(String keyword, SearchType searchType, Pageable pageable);

    PostResponse getPostByPostId(Long postId);

}
