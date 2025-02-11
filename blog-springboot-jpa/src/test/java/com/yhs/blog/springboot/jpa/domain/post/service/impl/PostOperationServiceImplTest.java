package com.yhs.blog.springboot.jpa.domain.post.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryWithChildrenResponse;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.featured_image.repository.FeaturedImageRepository;
import com.yhs.blog.springboot.jpa.domain.featured_image.service.FeaturedImageService;
import com.yhs.blog.springboot.jpa.domain.file.service.FileService;
import com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3.S3Service;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.FeaturedImageRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.Tag;
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
    FeaturedImageService featuredImageService;

    @Mock
    private FileService fileService;

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
    private RedisTemplate<String, List<CategoryWithChildrenResponse>> categoryResponseRedisTemplate;

    @InjectMocks
    private PostOperationServiceImpl postOperationService;

    BlogUser blogUser;

    @BeforeEach
    void setUp() {

        blogUser = new BlogUser(
                "testBlogId",
                "testUsername",
                1L,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
        );
    }

    @Nested
    @DisplayName("게시글 작성, 삭제, 수정 테스트 내부 클래스")
    class PostCrudTest {

        private Post post;

        @BeforeEach
        void setUp() {
            User user = TestUserFactory.createTestUserWithId();
            post = TestPostFactory.createTestPostWithId(user.getId());
        }

        @Test
        @DisplayName("특정 사용자의 게시글 작성 테스트")
        void 게시글_작성_테스트() {

            try (MockedStatic<TransactionSynchronizationManager> mockedStatic = mockStatic(
                    TransactionSynchronizationManager.class)) {

                // Given

                PostRequest postRequest = new PostRequest();
                postRequest.setTitle("Test Title");
                postRequest.setContent("Test Content");
                postRequest.setPostStatus("PUBLIC");
                postRequest.setCommentsEnabled("ALLOW");
                // 태그 설정 내부적으로 tag와 관련된 private 메서드를 실행하기 때문
                postRequest.setTags(Collections.emptyList());

                when(postRepository.save(any(Post.class))).thenReturn(post);

                // 대표 이미지 id 외래키 처리, doNothing()은 void메서드에만 가능
                when(featuredImageService.processFeaturedImageForCreatePostRequest(any()))
                        .thenReturn(1L);
                doNothing().when(fileService).processCreateFilesForCreatePostRequest(any(), any());

                // When
                postOperationService.createNewPost(postRequest, blogUser);

                // Then
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

                // Given
                Long postId = 1L;
                List<String> fileUrls = Arrays.asList("file1.jpg", "file2.jpg");
                String featuredImageUrl = "featured.jpg";
                List<Tag> unusedTags = Arrays.asList(new Tag("tag1"), new Tag("tag2"));

                // Post 조회 모킹
                when(postRepository.findById(postId)).thenReturn(Optional.of(post));

                // 파일 삭제 처리 모킹
                when(fileService.processDeleteFilesForDeletePostRequest(postId))
                        .thenReturn(fileUrls);

                // 대표 이미지 처리 모킹
                when(postRepository.findFeaturedImageIdByPostId(postId)).thenReturn(1L);
                when(featuredImageService.processDeleteFeaturedImageForDeletePostRequest(1L))
                        .thenReturn(featuredImageUrl);

                // 태그 처리 모킹
                doNothing().when(postTagRepository).deletePostTagsByPostId(postId);
                when(tagRepository.findUnusedTagsByPostId(postId)).thenReturn(Collections.emptyList());

                // When
                postOperationService.deletePostByPostId(postId, blogUser);

                // Then
                verify(postRepository).findById(postId);
                verify(fileService).processDeleteFilesForDeletePostRequest(postId);
                verify(postRepository).findFeaturedImageIdByPostId(postId);
                verify(featuredImageService).processDeleteFeaturedImageForDeletePostRequest(1L);
                verify(postTagRepository).deletePostTagsByPostId(postId);
                verify(tagRepository).findUnusedTagsByPostId(postId);
                verify(tagRepository, never()).deleteAllInBatch(unusedTags);
                verify(postRepository).delete(post);

                // 트랜잭션 후처리 검증
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
                Long userId = 1L;

                // 수정 요청 데이터 설정
                PostUpdateRequest updateRequest = new PostUpdateRequest(
                        null, // categoryName
                        "Updated Title", // title
                        "Updated Content", // content
                        new ArrayList<>(), // tags
                        new ArrayList<>(), // editPageDeletedTags
                        new ArrayList<>(), // files
                        new ArrayList<>(), // deletedImageUrlsInFuture
                        "PUBLIC", // postStatus
                        "ALLOW", // commentsEnabled
                        null // featuredImage
                );

                // 모킹 설정
                when(postRepository.findById(postId)).thenReturn(Optional.of(post));
                doNothing().when(fileService)
                        .processUpdateFilesForUpdatePostRequest(
                                any(List.class), eq(postId), any(List.class));
                when(featuredImageService.processFeaturedImageForUpdatePostRequest(any()))
                        .thenReturn(1L);

                // When
                postOperationService.updatePostByPostId(postId, blogUser, updateRequest);

                // Then
                verify(postRepository).findById(postId);
                verify(categoryService, never()).findCategoryByNameAndUserId(
                        updateRequest.getCategoryName(), userId);
                verify(fileService).processUpdateFilesForUpdatePostRequest(
                        any(List.class), eq(postId), any(List.class));
                verify(featuredImageService).processFeaturedImageForUpdatePostRequest(any());

                // 트랜잭션 후처리 검증
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

            // PostRequest 설정
            PostRequest postRequest = new PostRequest();
            postRequest.setTitle("Test Title");
            postRequest.setContent("Test Content");
            postRequest.setPostStatus("PUBLIC");
            postRequest.setCommentsEnabled("ALLOW");
            postRequest.setTags(Collections.emptyList());
            postRequest.setFiles(new ArrayList<>());

            // featuredImage 설정
            FeaturedImageRequest featuredImageRequest = new FeaturedImageRequest();
            featuredImageRequest.setFileUrl("test-image.jpg");
            featuredImageRequest.setFileName("test-file-name.jpg");
            featuredImageRequest.setFileType("image/jpeg");
            featuredImageRequest.setFileSize(1000L);
            postRequest.setFeaturedImage(featuredImageRequest);

            // featuredImage 처리 모킹
            when(featuredImageService.processFeaturedImageForCreatePostRequest(any()))
                    .thenReturn(1L);

            // DB 예외 발생 시뮬레이션
            when(postRepository.save(any(Post.class)))
                    .thenThrow(new DataAccessException("DB Error") {
                    });

            // When & Then
            SystemException exception = assertThrows(SystemException.class,
                    () -> postOperationService.createNewPost(postRequest, blogUser));

            // 예외 메시지 검증 (선택적)
            assertEquals("게시글 생성 중 서버 에러가 발생했습니다.", exception.getMessage());
        }
    }

}
