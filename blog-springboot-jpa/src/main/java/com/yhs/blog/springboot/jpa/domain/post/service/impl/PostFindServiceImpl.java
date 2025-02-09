package com.yhs.blog.springboot.jpa.domain.post.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostIndexAndIndexSearchResponse; 
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForDetailPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForEditPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostUserPageResponse;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.post.service.PostFindService;
import com.yhs.blog.springboot.jpa.domain.user.service.UserFindService;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostFindServiceImpl implements PostFindService {

    private final CategoryService categoryService;
    private final PostRepository postRepository;
    private final UserFindService userFindService;

    @Override
    @Transactional(readOnly = true)
    public Page<PostUserPageResponse> getAllPostsSpecificUser(String blogId, String keyword, SearchType searchType,
            String categoryName,
            Pageable pageable) {

        log.info(
                "[PostFindServiceImpl] getAllPostsSpecificUser 메서드 시작: blogId: {}, keyword: {}, searchType: {}, categoryName: {}, pageable: {}",
                blogId, keyword, searchType, categoryName, pageable);

        Long userId = userFindService.findUserByBlogId(blogId).getId();

        if (categoryName != null) {

            log.info("[PostFindServiceImpl] getAllPostsSpecificUser 카테고리 존재");

            String categoryId = categoryService.findCategoryByNameAndUserId(categoryName, userId).getId();

            return postRepository.findPostsByUserIdAndCategoryId(userId, categoryId, keyword, searchType, pageable);
        }

        log.info("[PostFindServiceImpl] getAllPostsSpecificUser 카테고리 미존재");

        return postRepository.findPostsByUserId(userId, keyword, searchType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostIndexAndIndexSearchResponse> getAllPostsAllUser(String keyword, SearchType searchType, Pageable pageable) {

        log.info("[PostFindServiceImpl] getAllPostsAllUser 메서드 시작: keyword: {}, searchType: {}, pageable: {}", keyword,
                searchType, pageable);

        return postRepository.findPostsAllUser(keyword, searchType, pageable);
    }

    @Loggable
    @Override
    @Transactional(readOnly = true)
    public PostResponseForDetailPage getPostByPostIdForDetailPage(Long postId) {

        log.info("[PostFindServiceImpl] getPostByPostId 메서드 시작: postId: {}", postId);

        PostResponseForDetailPage postResponseForDetailPage = Optional.ofNullable(postRepository.findByIdNotWithFeaturedImage(postId))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.POST_NOT_FOUND,
                        postId + "번 게시글을 찾을 수 없습니다.",
                        "PostFindServiceImpl",
                        "getPostByPostIdForDetailPage"));

        return postResponseForDetailPage;

    }

    @Loggable
    @Override
    @Transactional(readOnly = true)
    public PostResponseForEditPage getPostByPostIdForEditPage(Long postId) {

        log.info("[PostFindServiceImpl] getPostByPostId 메서드 시작: postId: {}", postId);

        PostResponseForEditPage postResponseFroEditPage = Optional.ofNullable(postRepository.findByIdWithFeaturedImage(postId))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.POST_NOT_FOUND,
                        postId + "번 게시글을 찾을 수 없습니다.",
                        "PostFindServiceImpl",
                        "getPostByPostId"));

        return postResponseFroEditPage;

    }

}
