package com.yhs.blog.springboot.jpa.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostIndexAndIndexSearchResponse; 
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForDetailPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForEditPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostUserPageResponse;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;

public interface PostRepositoryCustom {
    Page<PostUserPageResponse> findPostsByUserId(Long userId, String keyword, SearchType searchType, Pageable pageable);

    Page<PostUserPageResponse> findPostsByUserIdAndCategoryId(Long userId, String categoryUuid, String keyword,
            SearchType searchType, Pageable pageable);

    Page<PostIndexAndIndexSearchResponse> findPostsAllUser(String keyword, SearchType searchType, Pageable pageable);

    PostResponseForDetailPage findByIdNotWithFeaturedImage(Long postId);

    PostResponseForEditPage findByIdWithFeaturedImage(Long postId);

}
