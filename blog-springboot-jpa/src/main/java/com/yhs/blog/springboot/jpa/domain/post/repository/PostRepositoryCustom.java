package com.yhs.blog.springboot.jpa.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostAdminPageResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostIndexAndIndexSearchResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForDetailPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForEditPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostUserPageResponse;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;

public interface PostRepositoryCustom {

    Page<PostIndexAndIndexSearchResponse> findPostsForUserWithIndexPage(String keyword, SearchType searchType,
            Pageable pageable, Long userIdFromRefreshToken);

    Page<PostAdminPageResponse> findPostsByUserIdForAdminWithUserPage(Long userId, String keyword,
            SearchType searchType, Pageable pageable);
    Page<PostUserPageResponse> findPostsByUserIdForUserWithUserPage(Long userId, String keyword, SearchType searchType,
            Pageable pageable);

    Page<PostAdminPageResponse> findPostsByUserIdAndCategoryIdForAdminWithUserPage(Long userId, String categoryUuid,
            String keyword,
            SearchType searchType, Pageable pageable);
            
    Page<PostUserPageResponse> findPostsByUserIdAndCategoryIdForUserWithUserPage(Long userId, String categoryUuid,
            String keyword,
            SearchType searchType, Pageable pageable);

    Optional<PostResponseForDetailPage> findByIdNotWithFeaturedImage(Long postId);
    Optional<PostResponseForEditPage> findByIdWithFeaturedImage(Long postId);

}
