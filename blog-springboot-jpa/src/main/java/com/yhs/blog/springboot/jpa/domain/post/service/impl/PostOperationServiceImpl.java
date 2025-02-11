package com.yhs.blog.springboot.jpa.domain.post.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryWithChildrenResponse;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.featured_image.service.FeaturedImageService;
import com.yhs.blog.springboot.jpa.domain.file.service.FileService;
import com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3.S3Service;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.PostTag;
import com.yhs.blog.springboot.jpa.domain.post.entity.Tag;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostTagRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.TagRepository;
import com.yhs.blog.springboot.jpa.domain.post.service.PostOperationService;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostOperationServiceImpl implements PostOperationService {

    private final CategoryService categoryService;
    private final S3Service s3Service;
    private final FileService fileService;
    private final FeaturedImageService featuredImageService;

    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final TagRepository tagRepository;

    private final RedisTemplate<String, List<CategoryWithChildrenResponse>> categoryResponseRedisTemplate;

    @Loggable
    @Transactional
    @Override
    public void createNewPost(PostRequest postRequest, BlogUser blogUser) {

        log.info("[PostOperationServiceImpl] createNewPost 메서드 시작");

        // 여기까지 토큰이 통과했다는건 해당 사용자라는 뜻. jwt필터 및 컨트롤러에서 토큰 검증
        String blogId = blogUser.getBlogIdFromToken();
        Long userId = blogUser.getUserIdFromToken();

        try {

            String categoryId;
            if (postRequest.getCategoryName() != null) {

                log.info("[PostOperationServiceImpl] createNewPost 카테고리 존재 분기 시작");

                categoryId = categoryService.findCategoryByNameAndUserId(postRequest.getCategoryName(), userId).getId();
            } else {

                log.info("[PostOperationServiceImpl] createNewPost 카테고리 미존재 분기 시작");

                categoryId = null;
            }

            // 대표 이미지 id 외래키 처리
            Long featuredImageId = featuredImageService
                    .processFeaturedImageForCreatePostRequest(postRequest.getFeaturedImage());

            // temp -> final로 변환
            String convertedContent = postRequest.getContent().replace(blogId + "/temp/",
                    blogId + "/final/");

            Post post = Post.builder().userId(userId).categoryId(categoryId).title(postRequest.getTitle())
                    .content(convertedContent)
                    .postStatus(PostStatus.valueOf(postRequest.getPostStatus().toUpperCase()))
                    .commentsEnabled(CommentsEnabled.valueOf(postRequest.getCommentsEnabled().toUpperCase()))
                    .featuredImageId(featuredImageId).build();

            Post savedPost = postRepository.save(post);

            // 파일 처리
            // 아래 파일과 PostTag엔티티는 외래키에 postId가 있기 때문에 save로 저장한 뒤에 영속성 컨텍스트에 저장된 postId의 값을
            // 전달해야 함
            fileService.processCreateFilesForCreatePostRequest(postRequest.getFiles(), savedPost.getId());

            // 태그 처리
            processPostTagsAndTags(postRequest.getTags(), savedPost.getId());

            log.info("[PostOperationServiceImpl] createNewPost S3 메인 스레드 시작: {}", Thread.currentThread().getName());

            // 트랜잭션이 성공되어야만 실행
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {

                            // 카테고리 작업 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화. 비동기 작업이 실패하든 성공하든 얘는 무조건 실행
                            categoryResponseRedisTemplate.delete("categories:" + blogId);
                            // s3 Temp 파일 관련 작업은 비동기로 처리. 사용자에게 빠르게 응답하기 위함
                            // DB 작업 성공 후 S3관련 작업이 실패한다면: AWSS3 서버가 마비 되는거 아닌 이상 발생할 이유 없지만, 만약 발생한다면 삭제될 파일
                            // 제외하고 실제 DB에 저장된 요청온 파일을 따로
                            // DB 테이블에 저장하고 이후 temp -> final로 옮기는 배치 작업을 추가하면 됨. or 비동기 작업이 실패하면 요청 DTO를 활용해
                            // DB에서 DTO와 일치하는 엔티티 가져온 후 업데이트 하면 됨. 나중에 추가 고려
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
    public void updatePostByPostId(Long postId, BlogUser blogUser,
            PostUpdateRequest postUpdateRequest) {

        log.info("[PostOperationServiceImpl] updatePostByPostId 메서드 시작");

        String blogId = blogUser.getBlogIdFromToken();
        Long userId = blogUser.getUserIdFromToken();

        try {

            // 아래 수정할때 조회 -> 수정 대신 바로 게시글 수정할수도 있는데, 조회할때 찾지 못하면 예외 발생 시켜야 하기 때문에 조회 -> 수정
            // 처리
            // fetch join으로 user까지 함께 가져와 영속성 컨텍스트에 저장. 이후 getUser()로 가져올때 추가 select 쿼리 없음
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, postId +
                            "번 게시글을 찾을 수 없습니다.",
                            "PostOperationServiceImpl", "updatePostByPostId"));

            String categoryId;
            if (postUpdateRequest.getCategoryName() != null) {
                log.info("[PostOperationServiceImpl] updatePostByPostId 카테고리 존재 분기 시작");

                categoryId = categoryService.findCategoryByNameAndUserId(postUpdateRequest.getCategoryName(), userId)
                        .getId();

            } else {

                log.info("[PostOperationServiceImpl] updatePostByPostId 카테고리 미존재 분기 시작");
                categoryId = null;
            }

            // 파일(이미지) 삭제 및 저장 처리 - 대표 이미지 포함
            fileService.processUpdateFilesForUpdatePostRequest(postUpdateRequest.getFiles(), postId,
                    postUpdateRequest.getDeletedImageUrlsInFuture());

            // 태그 처리
            if (postUpdateRequest.getEditPageDeletedTags() != null
                    && !postUpdateRequest.getEditPageDeletedTags().isEmpty()) {

                log.info(
                        "[PostOperationServiceImpl] updatePostByPostId !postUpdateRequest.getEditPageDeletedTags().isEmpty() 분기 시작");

                // POSTTAG 삭제, 외래키 문제가 있을 수 있어 posttag 먼저 삭제 해주어야 함
                postTagRepository.deleteByPostIdAndTagNames(postId, postUpdateRequest.getEditPageDeletedTags());

                // TAG 삭제
                List<Tag> unusedTags = tagRepository.findUnusedTagsByTagNames(
                        postUpdateRequest.getEditPageDeletedTags(), postId);
                tagRepository.deleteAll(unusedTags);
            }

            processPostTagsAndTags(postUpdateRequest.getTags(), postId);

            // 대표 이미지 설정, 대표 이미지 삭제는 위쪽 fileService.processUpdateFilesForUpdatePostRequest에서
            // 일괄 처리 했음
            Long featuredImageId = featuredImageService
                    .processFeaturedImageForUpdatePostRequest(postUpdateRequest.getFeaturedImage());

            // content temp -> final로 변환
            String convertedContent = postUpdateRequest.getContent().replace(blogId + "/temp/",
                    blogId + "/final/");

            // 더티 체킹 대상으로써 save 메서드 불필요.
            post.update(categoryId, featuredImageId, postUpdateRequest.getTitle(), convertedContent,
                    PostStatus.valueOf(postUpdateRequest.getPostStatus().toUpperCase()),
                    CommentsEnabled.valueOf(postUpdateRequest.getCommentsEnabled().toUpperCase()));

            // 트랜잭션이 성공되어야만 실행
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {

                            // 카테고리 작업 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화. 비동기 작업이 실패하든 성공하든 얘는 무조건 실행
                            categoryResponseRedisTemplate.delete("categories:" + blogId);
                            // s3 Temp 파일 관련 작업은 비동기로 처리. 사용자에게 빠르게 응답하기 위함
                            s3Service.processUpdatePostS3TempOperation(postUpdateRequest, blogId);

                        }

                    });
        } catch (DataAccessException ex) {
            throw new SystemException(ErrorCode.POST_CREATE_ERROR, "게시글 업데이트 중 서버 에러가 발생했습니다.",
                    "PostOperationServiceImpl", "updatePostByPostId", ex);
        }

    }

    @Loggable
    @Transactional
    @Override
    public void deletePostByPostId(Long postId, BlogUser blogUser) {
        log.info("[PostOperationServiceImpl] deletePostByPostId 메서드 시작: postId: {}", postId);

        try {

            // 아래 삭제할때 조회 -> 삭제 대신 바로 삭제할수도 있는데, 조회할때 찾지 못하면 예외 발생 시켜야 하기 때문에 조회 -> 삭제

            String blogId = blogUser.getBlogIdFromToken();

            Post foundPost = postRepository.findById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, postId + "번 게시글을 찾을 수 없습니다.",
                            "PostOperationServiceImpl", "deletePostByPostId"));

            List<String> toBeDeletedFileUrls = new ArrayList<>();

            // 파일 삭제 처리
            List<String> fileUrls = fileService.processDeleteFilesForDeletePostRequest(postId);

            if (fileUrls != null && !fileUrls.isEmpty()) {
                toBeDeletedFileUrls.addAll(fileUrls);
            }

            // 대표 이미지 삭제 처리
            Long featuredImageId = postRepository.findFeaturedImageIdByPostId(postId);

            if (featuredImageId != null) {
                String featuredImageFileUrl = featuredImageService
                        .processDeleteFeaturedImageForDeletePostRequest(featuredImageId);
                toBeDeletedFileUrls.add(featuredImageFileUrl);
            }

            // 태그 삭제 처리, PostTag는 무조건 삭제하면 됨
            postTagRepository.deletePostTagsByPostId(postId);
            List<Tag> toBeDeletedTags = tagRepository.findUnusedTagsByPostId(postId);

            if (toBeDeletedTags != null && !toBeDeletedTags.isEmpty()) {
                tagRepository.deleteAllInBatch(toBeDeletedTags);
            }

            // 게시글 삭제 처리

            postRepository.delete(foundPost);

            // 트랜잭션이 성공되어야만 실행
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            s3Service.processDeletePostS3Operation(toBeDeletedFileUrls, blogId);
                            // 카테고리 작업 시 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화
                            categoryResponseRedisTemplate.delete("categories:" + blogId);
                        }

                    });
        } catch (DataAccessException ex) {
            throw new SystemException(ErrorCode.POST_CREATE_ERROR, "게시글 삭제 중 서버 에러가 발생했습니다.",
                    "PostOperationServiceImpl", "deletePostByPostId", ex);
        }

    }

    private void processPostTagsAndTags(List<String> tagNames, Long postId) {

        log.info("[PostOperationServiceImpl] processTags 메서드 시작");

        if (tagNames != null && !tagNames.isEmpty()) {

            log.info("[PostOperationServiceImpl] processTags !tagNames.isEmpty() 분기 시작");

            List<PostTag> postTags = new ArrayList<>();
            List<Tag> tags = new ArrayList<>();

            for (String tagName : tagNames) {
                Optional<Tag> tag = tagRepository.findByName(tagName);

                if (tag.isPresent()) {
                    postTags.add(new PostTag(postId, tag.get().getId()));
                    continue;
                }

                Tag newTag = new Tag(tagName);

                tags.add(newTag);
            }

            List<Tag> savedTags = tagRepository.saveAll(tags);

            savedTags.forEach(tag -> {
                PostTag postTag = new PostTag(postId, tag.getId());
                postTags.add(postTag);
            });

            postTagRepository.saveAll(postTags);
        } else {
            return;
        }
    }

}
