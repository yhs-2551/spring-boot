package com.yhs.blog.springboot.jpa.domain.post.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.auth.token.claims.ClaimsExtractor;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostAdminAndUserBaseResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostIndexAndIndexSearchResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForDetailPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForEditPage;
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

    // PostAdminPageResponse, PostUserPageResponse 둘 중 하나 리턴
    @Override
    @Transactional(readOnly = true)
    public Page<? extends PostAdminAndUserBaseResponse> getAllPostsSpecificUser(String blogId, String keyword,
            SearchType searchType,
            String categoryName,
            Pageable pageable, BlogUser blogUser) {

        log.info(
                "[PostFindServiceImpl] getAllPostsSpecificUser 메서드 시작");

        // 카테고리는 정확히 해당 블로그 주인의 카테고리를 가져와야 하기 때문에 토큰 기반이 아닌 경로변수로 받은 blogId를 통해서 사용자를
        // 찾아야함
        Long userId = userFindService.findUserByBlogId(blogId).getId();

        if (categoryName != null) {

            log.info("[PostFindServiceImpl] getAllPostsSpecificUser 카테고리 존재");

            String categoryId = categoryService.findCategoryByNameAndUserId(categoryName, userId).getId();

            if (blogUser != null) {

                String blogIdFromAuthenticatedBlogUser = blogUser.getBlogIdFromToken();

                if (blogIdFromAuthenticatedBlogUser.equals(blogId)) {
                    log.info("[PostFindServiceImpl] getAllPostsSpecificUser 블로그 주인일 때 분기 진행");

                    return postRepository.findPostsByUserIdAndCategoryIdForAdminWithUserPage(userId, categoryId,
                            keyword,
                            searchType, pageable);

                } else {

                    log.info("[PostFindServiceImpl] getAllPostsSpecificUser 블로그 주인이 아닐 때(로그인 사용자) 분기 진행");

                    return postRepository.findPostsByUserIdAndCategoryIdForUserWithUserPage(userId, categoryId, keyword,
                            searchType, pageable);
                }

            } else {

                log.info("[PostFindServiceImpl] getAllPostsSpecificUser 블로그 주인이 아닐 때(비로그인 사용자) 분기 진행");

                return postRepository.findPostsByUserIdAndCategoryIdForUserWithUserPage(userId, categoryId, keyword,
                        searchType, pageable);

            }

        }

        log.info("[PostFindServiceImpl] getAllPostsSpecificUser 카테고리 미존재 분기 진행");

        if (blogUser != null) {

            String blogIdFromAuthenticatedBlogUser = blogUser.getBlogIdFromToken();

            if (blogIdFromAuthenticatedBlogUser.equals(blogId)) {

                log.info("[PostFindServiceImpl] getAllPostsSpecificUser 블로그 주인일 때 분기 진행");

                return postRepository.findPostsByUserIdForAdminWithUserPage(userId, keyword, searchType, pageable);
            } else {
                log.info("[PostFindServiceImpl] getAllPostsSpecificUser 블로그 주인이 아닐 때(로그인 사용자) 분기 진행");

                return postRepository.findPostsByUserIdForUserWithUserPage(userId, keyword, searchType, pageable);
            }

        } else {

            log.info("[PostFindServiceImpl] getAllPostsSpecificUser 블로그 주인이 아닐 때(비로그인 사용자) 분기 진행");

            return postRepository.findPostsByUserIdForUserWithUserPage(userId, keyword, searchType, pageable);

        }

    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostIndexAndIndexSearchResponse> getAllPostsAllUser(String keyword, SearchType searchType,
            Pageable pageable, BlogUser blogUser) {

        log.info("[PostFindServiceImpl] getAllPostsAllUser 메서드 시작: keyword: {}, searchType: {}, pageable: {}", keyword,
                searchType, pageable);

        if (blogUser != null) {

            Long userIdFromAuthenticatedBlogUser = blogUser.getUserIdFromToken();

            log.info("[PostFindServiceImpl] getAllPostsAllUser 블로그 주인일 때 분기 진행");

            return postRepository.findPostsForUserWithIndexPage(keyword, searchType, pageable,
                    userIdFromAuthenticatedBlogUser);

        } else {

            log.info("[PostFindServiceImpl] getAllPostsAllUser 블로그 주인이 아닐 때 분기 진행");

            return postRepository.findPostsForUserWithIndexPage(keyword, searchType, pageable, null);

        }

    }

    @Loggable
    @Override
    @Transactional(readOnly = true)
    public PostResponseForDetailPage getPostByPostIdForDetailPage(Long postId) {

        log.info("[PostFindServiceImpl] getPostByPostId 메서드 시작: postId: {}", postId);

        PostResponseForDetailPage postResponseForDetailPage = postRepository.findByIdNotWithFeaturedImage(postId)
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

        PostResponseForEditPage postResponseFroEditPage = postRepository.findByIdWithFeaturedImage(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.POST_NOT_FOUND,
                        postId + "번 게시글을 찾을 수 없습니다.",
                        "PostFindServiceImpl",
                        "getPostByPostId"));

        return postResponseFroEditPage;

    }

}
