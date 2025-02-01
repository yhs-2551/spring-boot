package com.yhs.blog.springboot.jpa.domain.post.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set; 
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.file.dto.request.FileRequest;
import com.yhs.blog.springboot.jpa.domain.file.entity.File;
import com.yhs.blog.springboot.jpa.domain.file.mapper.FileMapper;
import com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3.S3Service;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.FeaturedImageRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.PostTag;
import com.yhs.blog.springboot.jpa.domain.post.entity.Tag;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.mapper.PostMapper;
import com.yhs.blog.springboot.jpa.domain.post.repository.FeaturedImageRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostTagRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.TagRepository;
import com.yhs.blog.springboot.jpa.domain.post.service.PostOperationService;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.service.UserFindService;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostOperationServiceImpl implements PostOperationService {

    private final UserFindService userFindService;
    private final CategoryService categoryService;
    private final PostRepository postRepository;
    private final FeaturedImageRepository featuredImageRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final S3Service s3Service;
    private final RedisTemplate<String, List<CategoryResponse>> categoryResponseRedisTemplate;

    @Loggable
    @Transactional
    @Override
    public void createNewPost(PostRequest postRequest, String blogId) {

        log.info("[PostOperationServiceImpl] createNewPost 메서드 시작:  blogId: {}", blogId);

        try {

            User user = userFindService.findUserByBlogId(blogId);

            Category category;
            if (postRequest.getCategoryName() != null) {

                log.info("[PostOperationServiceImpl] createNewPost 카테고리 존재 분기 시작");

                category = categoryService.findCategoryByNameAndUserId(postRequest.getCategoryName(), user.getId());
            } else {

                log.info("[PostOperationServiceImpl] createNewPost 카테고리 미존재 분기 시작");

                category = null;
            }

            FeaturedImage featuredImage = processFeaturedImage(postRequest.getFeaturedImage());

            // temp -> final로 변환
            String convertedContent = postRequest.getContent().replace(blogId + "/temp/",
                    blogId + "/final/");

            // 포스트 생성 시 user를 넘겨주면 외래키 연관관계 설정으로 인해 posts테이블에 user_id 값이 자동으로 들어간다.
            // category, featuredImage또한 마찬가지.
            Post post = PostMapper.create(user, category, postRequest.getTitle(), convertedContent,
                    postRequest.getPostStatus(), postRequest.getCommentsEnabled(), featuredImage);
            post.setFiles(processFiles(post, postRequest.getFiles(), user));
            post.setPostTags(processTags(post, postRequest.getTags(), user));
            postRepository.save(post);

            log.info("[PostOperationServiceImpl] createNewPost S3 메인 스레드 시작: {}", Thread.currentThread().getName());

            // 트랜잭션이 성공되어야만 실행
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            // 카테고리 작업 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화
                            categoryResponseRedisTemplate.delete("categories:" + blogId);
                            // s3 Temp 파일 관련 작업은 비동기로 처리. 사용자에게 빠르게 응답하기 위함
                            s3Service.processCreatePostS3TempOperation(postRequest,
                                    blogId);
                        }

                    });

        } catch (DataAccessException ex) {
            throw new SystemException(ErrorCode.POST_CREATE_ERROR, "게시글 생성 중 서버 에러가 발생했습니다.",
                    "PostOperationServiceImpl", "createNewPost", ex);
        }
    }

    // 업데이트의 경우 가져온 엔티티를 스냅샷으로 저장하여 더티체킹을 통해 업데이트를 진행한다.
    // 쉽게 React 가상 DOM과 비슷한 개념으로 이해하면 된다.
    @Loggable
    @Transactional
    @Override
    public void updatePostByPostId(Long postId, String blogId,
            PostUpdateRequest postUpdateRequest) {

        log.info("[PostOperationServiceImpl] updatePostByPostId 메서드 시작: postId: {}, blogId: {}",
                postId, blogId);

        // fetch join으로 user까지 함께 가져와 영속성 컨텍스트에 저장. 이후 getUser()로 가져올때 추가 select 쿼리 없음
        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, postId +
                        "번 게시글을 찾을 수 없습니다.",
                        "PostOperationServiceImpl", "updatePostByPostId"));

        User user = post.getUser();

        Category category;
        if (postUpdateRequest.getCategoryName() != null) {
            log.info("[PostOperationServiceImpl] updatePostByPostId 카테고리 존재 분기 시작");

            category = categoryService.findCategoryByNameAndUserId(postUpdateRequest.getCategoryName(), user.getId());

        } else {

            log.info("[PostOperationServiceImpl] updatePostByPostId 카테고리 미존재 분기 시작");
            category = null;
        }

        Set<File> newFiles = processFiles(post, postUpdateRequest.getFiles(), user);
        List<PostTag> newPostTags = processTags(post, postUpdateRequest.getTags(), user);

        if (postUpdateRequest.getEditPageDeletedTags() != null
                && !postUpdateRequest.getEditPageDeletedTags().isEmpty()) {

            log.info(
                    "[PostOperationServiceImpl] updatePostByPostId !postUpdateRequest.getEditPageDeletedTags().isEmpty() 분기 시작");

            List<Tag> unusedTags = tagRepository.findUnusedTagsNotUsedByOtherPostsAndOtherUsers(
                    postUpdateRequest.getEditPageDeletedTags(), postId, user.getId());

            tagRepository.deleteAll(unusedTags);
        }

        FeaturedImage featuredImage = processFeaturedImage(postUpdateRequest.getFeaturedImage());

        // temp -> final로 변환
        String convertedContent = postUpdateRequest.getContent().replace(user.getBlogId() + "/temp/",
                user.getBlogId() + "/final/");

        // 더티 체킹 대상으로써 save 메서드 불필요
        post.update(category, postUpdateRequest.getTitle(), convertedContent, newFiles, newPostTags,
                PostStatus.valueOf(postUpdateRequest.getPostStatus().toUpperCase()),
                CommentsEnabled.valueOf(postUpdateRequest.getCommentsEnabled().toUpperCase()), featuredImage);

        // 트랜잭션이 성공되어야만 실행
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 카테고리 작업 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화
                        categoryResponseRedisTemplate.delete("categories:" + blogId);
                        // s3 Temp 파일 관련 작업은 비동기로 처리. 사용자에게 빠르게 응답하기 위함
                        s3Service.processUpdatePostS3TempOperation(postUpdateRequest, user.getBlogId());
                    }

                });

    }

    @Loggable
    @Transactional
    @Override
    public void deletePostByPostId(Long postId, String blogId) {

        log.info("[PostOperationServiceImpl] deletePostByPostId 메서드 시작: postId: {}", postId);

        Post post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, postId + "번 게시글을 찾을 수 없습니다.",
                        "PostOperationServiceImpl", "deletePostByPostId"));

        Long userId = post.getUser().getId();

        // 삭제될 포스트의 태그 정보 미리 저장
        List<Long> postTagIds = postTagRepository.findTagIdsByPostId(postId);

        // Post를 삭제하면서 cascade효과로 인해 관련된 PostTag 삭제.
        // 위에서 영속성 컨텍스트로 로드된 post를 이용하여 삭제
        postRepository.delete(post);

        // 해당 사용자와 포스트에서만 사용된 태그 삭제
        if (!postTagIds.isEmpty()) {

            log.info("[PostOperationServiceImpl] deletePostByPostId !postTagIds.isEmpty() 분기 시작");

            tagRepository.deleteUnusedTags(postTagIds, postId, userId);
        }

        // tag 먼저 삭제하고 posttag를 삭제하면, 외래키 오류나고,
        // 외래키 오류를 피하기 위해 posttag 먼저 삭제하고 tag를 삭제하면 tag에서 PostTag 엔티티 관련된 JPQL Query문 실행
        // 시 posttag가
        // 영속성 컨텍스트에서 삭제되었기 PostTag엔티티 관련된 JPQL실행이 제대로 되지 않아서이다.
        // 따라서 최종적으로 위쪽 방식으로 사용한다.

        // 트랜잭션이 성공되어야만 실행
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 카테고리 작업 시 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화
                        categoryResponseRedisTemplate.delete("categories:" + blogId);
                    }

                });
    }

    // 아래쪽은 헬퍼 메서드
    private Set<File> processFiles(Post post, List<FileRequest> fileRequests, User user) {

        log.info("[PostOperationServiceImpl] processFiles 메서드 시작");

        Set<File> files = new HashSet<>();
        if (fileRequests != null && !fileRequests.isEmpty()) {

            log.info("[PostOperationServiceImpl] processFiles !fileRequests.isEmpty() 분기 시작");

            for (FileRequest fileRequest : fileRequests) {

                // 최종적으로 post를 저장시에 aws 파일 저장 위치를 temp -> final로 변경하기 때문에 final로 변경하는 로직 추가.
                // 따라서 db에 final 경로로 저장한다.
                String updatedFileUrl = fileRequest.getFileUrl().replace("/temp/", "/final/");

                File file = FileMapper.create(fileRequest, post, user, updatedFileUrl);
                files.add(file);
            }
        }
        return files;
    }

    private List<PostTag> processTags(Post post, List<String> tagNames, User user) {

        log.info("[PostOperationServiceImpl] processTags 메서드 시작");

        List<PostTag> postTags = new ArrayList<>();
        if (tagNames != null && !tagNames.isEmpty()) {
            log.info("[PostOperationServiceImpl] processTags !tagNames.isEmpty() 분기 시작");
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByName(tagName).orElseGet(() -> Tag.create(tagName));
                tagRepository.save(tag);
                PostTag postTag = PostTag.create(post, tag, user);
                postTags.add(postTag);
            }
        }
        return postTags;
    }

    private FeaturedImage processFeaturedImage(FeaturedImageRequest featuredImageRequest) {

        log.info("[PostOperationServiceImpl] processFeaturedImage 메서드 시작");

        if (featuredImageRequest != null) {

            log.info("[PostOperationServiceImpl] processFeaturedImage featuredImageRequest != null 분기 시작");

            // 최종적으로 post를 저장시에 aws 파일 저장 위치를 temp -> final로 변경하기 때문에 final로 변경하는 로직 추가. 따라서
            // db에 final 경로로 저장한다
            String updatedFileUrl = featuredImageRequest.getFileUrl().replace("/temp/", "/final/");
            FeaturedImage featuredImage = FeaturedImage.builder()
                    .fileName(featuredImageRequest.getFileName())
                    .fileUrl(updatedFileUrl)
                    .fileType(featuredImageRequest.getFileType())
                    .fileSize(featuredImageRequest.getFileSize())
                    .build();
            featuredImageRepository.save(featuredImage);
            return featuredImage;
        }
        return null;
    }

}
