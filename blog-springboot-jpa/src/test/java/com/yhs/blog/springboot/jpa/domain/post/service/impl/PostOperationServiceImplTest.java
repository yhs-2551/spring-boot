package com.yhs.blog.springboot.jpa.domain.post.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.featured_image.repository.FeaturedImageRepository;
import com.yhs.blog.springboot.jpa.domain.file.dto.request.FileRequest;
import com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3.S3Service;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.FeaturedImageRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.factory.TestPostFactory;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostTagRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.TagRepository;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.service.UserFindService;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

@ExtendWith(MockitoExtension.class)
public class PostOperationServiceImplTest {

    @Mock
    private UserFindService userFindService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private FeaturedImageRepository featuredImageRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private PostTagRepository postTagRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private RedisTemplate<String, List<CategoryResponse>> categoryResponseRedisTemplate;

    @InjectMocks
    private PostOperationServiceImpl postOperationService;

    @Nested
    @DisplayName("게시글 작성, 삭제, 수정 테스트 내부 클래스")
    class PostCrudTest {

        private User user;
        private Post post;

        @BeforeEach
        void setUp() {
            user = TestUserFactory.createTestUserWithId();
            post = TestPostFactory.createTestPostWithId(user);
        }

        @Test
        @DisplayName("특정 사용자의 게시글 작성 테스트")
        void 게시글_작성_테스트() {

            try (MockedStatic<TransactionSynchronizationManager> mockedStatic = mockStatic(
                    TransactionSynchronizationManager.class)) {

                // Given
                String blogId = "testBlogId";
                PostRequest postRequest = PostRequest.builder()
                        .title("Test Title")
                        .content("Test Content")
                        .postStatus("PUBLIC")
                        .commentsEnabled("ALLOW")
                        .build();

                when(userFindService.findUserByBlogId(blogId)).thenReturn(user);
                when(postRepository.save(any(Post.class))).thenReturn(post);

                // When
                postOperationService.createNewPost(postRequest, blogId);

                // Then
                verify(userFindService).findUserByBlogId(blogId);
                verify(postRepository).save(any(Post.class));

                mockedStatic.verify(() -> TransactionSynchronizationManager
                        .registerSynchronization(any(TransactionSynchronization.class)));
            }
        }

        @Test
        @DisplayName("특정 사용자의 게시글 삭제 테스트")
        void 게시글_삭제_테스트() {

            try (MockedStatic<TransactionSynchronizationManager> mockedStatic = mockStatic(
                    TransactionSynchronizationManager.class)) {

                Long postId = 1L;

                // Given
                String blogId = "testBlogId";

                when(postRepository.findByIdWithUser(postId)).thenReturn(Optional.of(post));
                when(postTagRepository.findTagIdsByPostId(postId)).thenReturn(Arrays.asList(1L,
                        2L));

                // When
                postOperationService.deletePostByPostId(postId, blogId);

                // Then
                verify(postRepository).findByIdWithUser(postId);
                verify(postRepository).delete(post);
                verify(tagRepository).deleteUnusedTags(anyList(), eq(postId),
                        eq(post.getUser().getId()));

                mockedStatic.verify(() -> TransactionSynchronizationManager
                        .registerSynchronization(any(TransactionSynchronization.class)));
            }
        }

        @Test
        @DisplayName("특정 사용자의 게시글 수정 테스트")
        void 게시글_수정_테스트() {

            try (MockedStatic<TransactionSynchronizationManager> mockedStatic = mockStatic(
                    TransactionSynchronizationManager.class)) {

                // Given
                Long postId = 1L;
                String blogId = "testBlogId";

                post.setFiles(new HashSet<>());
                post.setPostTags(new ArrayList<>());

                PostUpdateRequest updateRequest = new PostUpdateRequest(null, "Test Title",
                        "Test Content", null, null, null, null, "PUBLIC", "ALLOW", null);

                when(postRepository.findByIdWithUser(postId)).thenReturn(Optional.of(post));

                // When
                postOperationService.updatePostByPostId(postId, blogId, updateRequest);

                // Then
                verify(postRepository).findByIdWithUser(postId);

                mockedStatic.verify(() -> TransactionSynchronizationManager
                        .registerSynchronization(any(TransactionSynchronization.class)));
            }
        }
    }

    @Nested
    @DisplayName("게시글 작성 시 SystemException 발생 내부 클래스")
    class PostSystemExceptionTest {

        @Test
        @DisplayName("게시글 작성 시 SystemException 발생 테스트")
        void 게시글_작성시_SystemException_발생_테스트() {
            // Given
            String blogId = "testBlogId";
            PostRequest postRequest = new PostRequest();

            when(userFindService.findUserByBlogId(blogId))
                    .thenThrow(new DataAccessException("DB Error") {
                    });

            // Then
            assertThrows(SystemException.class, () -> {
                postOperationService.createNewPost(postRequest, blogId);
            });
        }
    }

}
