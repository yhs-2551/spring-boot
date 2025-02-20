package com.yhs.blog.springboot.jpa.domain.post.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForDetailPage;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.service.UserFindService;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;

// 아래 테스트는 블로그의 주인이 아닌 경우 즉, 비로그인 사용자 또는 A사용자가 B사용자의 블로그에 방문했을때를 가정. 즉 refresh token 값을 null로 한다
@ExtendWith(MockitoExtension.class)
public class PostFindServiceImplTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserFindService userFindService;

    @InjectMocks
    private PostFindServiceImpl postFindService;

    private User user;
    private Category category;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        category = Category.builder().id("category1").name("Test Category").build();
        user = TestUserFactory.createTestUserWithId();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("카테고리 있는 특정 사용자의 모든 게시글 조회 테스트")
    void 카테고리_있는_특정_사용자의_모든_게시글_조회() {
        // given
        when(userFindService.findUserByBlogId(anyString())).thenReturn(user);
        when(categoryService.findCategoryByNameAndUserId(anyString(), anyLong())).thenReturn(category);

        // when
        postFindService.getAllPostsSpecificUser("testBlogId", "keyword", SearchType.TITLE, "Test Category", pageable, null);

        // then
        verify(postRepository).findPostsByUserIdAndCategoryIdForUserWithUserPage(anyLong(), anyString(), anyString(),
                any(SearchType.class), any(Pageable.class));
    }

    @Test
    @DisplayName("카테고리 없는 특정 사용자의 모든 게시글 조회 테스트")
    void 카테고리_없는_특정_사용자의_모든_게시글_조회() {
        // given
        when(userFindService.findUserByBlogId(anyString())).thenReturn(user);

        // when
        postFindService.getAllPostsSpecificUser("testBlogId", "keyword", SearchType.TITLE, null, pageable, null);

        // then
        verify(postRepository).findPostsByUserIdForUserWithUserPage(anyLong(), anyString(), any(SearchType.class), any(Pageable.class));
    }

    @Test
    @DisplayName("전체 사용자의 게시글 조회 테스트")
    void 전체_사용자의_모든_게시글_조회() {
        // when
        postFindService.getAllPostsAllUser("keyword", SearchType.TITLE, pageable, null);

        // then
        verify(postRepository).findPostsForUserWithIndexPage(anyString(), any(SearchType.class), any(Pageable.class), isNull());
    }

    @Test
    @DisplayName("게시글 ID로 단일 게시글 조회 성공")
    void 특정_사용자의_단일_게시글_조회() {
        // given
        PostResponseForDetailPage postResponseForDetailPage = new PostResponseForDetailPage(null, null, null, null,
                PostStatus.PUBLIC, null, null, null);

        when(postRepository.findByIdNotWithFeaturedImage(anyLong())).thenReturn(Optional.of(postResponseForDetailPage));

        // when
        PostResponseForDetailPage result = postFindService.getPostByPostIdForDetailPage(1L);

        // then
        assertNotNull(result);
        verify(postRepository).findByIdNotWithFeaturedImage(1L);
    }

    @Test
    @DisplayName("게시글 ID로 단일 게시글 조회 실패 - 게시글 없음")
    void 특정_사용자의_단일_게시글_조회_실패() {
        // given
        when(postRepository.findByIdNotWithFeaturedImage(anyLong())).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> postFindService.getPostByPostIdForDetailPage(1L));
        assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }
}
