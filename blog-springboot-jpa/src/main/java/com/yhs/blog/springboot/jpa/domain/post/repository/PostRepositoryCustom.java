package com.yhs.blog.springboot.jpa.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;

public interface PostRepositoryCustom {
    Page<PostResponse> findPosts(Long userId, String keyword, SearchType searchType, Pageable pageable);
    Page<PostResponse> findPostsByCategory(Long userId, String categoryUuid, String keyword, SearchType searchType, Pageable pageable);

}
