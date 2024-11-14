package com.yhs.blog.springboot.jpa.service.impl;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.*;
import com.yhs.blog.springboot.jpa.entity.*;
import com.yhs.blog.springboot.jpa.repository.*;
import com.yhs.blog.springboot.jpa.service.PostService;
import com.yhs.blog.springboot.jpa.service.S3Service;
import com.yhs.blog.springboot.jpa.service.UserService;
import com.yhs.blog.springboot.jpa.util.PostMapper;
import com.yhs.blog.springboot.jpa.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostServiceImpl implements PostService {

    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;
    private final FeaturedImageRepository featuredImageRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final S3Service s3Service;

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPostListByUserId(Long userId) {
        List<Post> posts = postRepository.findByUserId(userId);
        return posts.stream().map(PostResponse::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostByPostId(Long postId) {
        Post post =  postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id " + postId));

        return new PostResponse(post);
    }

    @Transactional
    @Override
    public void deletePostByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id " + postId));
        Long userId = post.getUser().getId();

        // 삭제될 포스트의 태그 정보 미리 저장
        List<Long> postTagIds = postTagRepository.findTagIdsByPostId(postId);

        // Post를 삭제하면서 cascade효과로 인해 관련된 PostTag 삭제.
        postRepository.deleteById(postId);

        // 해당 사용자와 포스트에서만 사용된 태그 삭제
        if (!postTagIds.isEmpty()) {
            tagRepository.deleteUnusedTags(postTagIds, postId, userId);
        }

        // tag 먼저 삭제하고 posttag를 삭제하면,  외래키 오류나고,
        // posttag 먼저 삭제하고 tag를 삭제하면 tag에서 PostTag 엔티티 관련된 JPQL Query문 실행 시 posttag가 영속성 컨텍스트에서
        // 삭제되었기 PostTag엔티티 관련된 JPQL실행이 제대로 되지 않아서이다.



    }


    @Transactional
    @Override
    public PostResponse createNewPost(PostRequest postRequest, HttpServletRequest request) {
        try {

            log.info("PostRequest >>>> " + postRequest);
            Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
            User user = userService.findUserById(userId);

            Category category;
            if (postRequest.getCategoryName() != null) {
                category = categoryRepository.findByNameAndUserId(postRequest.getCategoryName(), userId)
                        .orElse(null);
            } else {
                category = null;
            }

            FeaturedImage featuredImage = processFeaturedImage(postRequest.getFeaturedImage());

            // 포스트 생성 시 user를 넘겨주면 외래키 연관관계 설정으로 인해 posts테이블에 user_id 값이 자동으로 들어간다.
            // category, featuredImage또한 마찬가지.
            Post post = PostMapper.toEntity(user, category, postRequest.getTitle(), postRequest.getContent(), postRequest.getPostStatus(), postRequest.getCommentsEnabled(), featuredImage);
            post.setFiles(processFiles(post, postRequest.getFiles(), user));
            post.setPostTags(processTags(post, postRequest.getTags(), user));

            postRepository.save(post);

            // s3 Temp 파일 관련 작업은 비동기로 처리. 사용자에게 빠르게 응답하기 위함
            s3Service.processCreatePostS3TempOperation(postRequest, user.getUserIdentifier());

            return new PostResponse(post);

        } catch (DataAccessException ex) {
            throw new RuntimeException("A server error occurred while creating the post.", ex);
        }
    }

    @Transactional
    @Override
    public Post updatePostByPostId(Long postId, Long userId,
                                   PostUpdateRequest postUpdateRequest) {

        User user = userService.findUserById(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id " + postId));

        Category category;
        if (postUpdateRequest.getCategoryName() != null) {
            category = categoryRepository.findByNameAndUserId(postUpdateRequest.getCategoryName(), userId)
                    .orElse(null);
        } else {
            category = null;
        }

        Set<File> newFiles = processFiles(post, postUpdateRequest.getFiles(), user);
        List<PostTag> newPostTags = processTags(post, postUpdateRequest.getTags(), user);

        if (postUpdateRequest.getEditPageDeletedTags() != null && !postUpdateRequest.getEditPageDeletedTags().isEmpty()) {

            List<Tag> unusedTags =
                    tagRepository.findUnusedTagsNotUsedByOtherPostsAndOtherUsers(postUpdateRequest.getEditPageDeletedTags(), postId, userId);

            tagRepository.deleteAll(unusedTags);
        }


        FeaturedImage featuredImage = processFeaturedImage(postUpdateRequest.getFeaturedImage());

        post.update(category, postUpdateRequest.getTitle(), postUpdateRequest.getContent(), newFiles, newPostTags, Post.PostStatus.valueOf(postUpdateRequest.getPostStatus().toUpperCase()), Post.CommentsEnabled.valueOf(postUpdateRequest.getCommentsEnabled().toUpperCase()), featuredImage);
        s3Service.processUpdatePostS3TempOperation(postUpdateRequest, user.getUserIdentifier());
        return postRepository.save(post);
    }

    //아래쪽은 헬퍼 메서드

    private Set<File> processFiles(Post post, List<FileRequest> fileRequests, User user) {
        Set<File> files = new HashSet<>();
        if (fileRequests != null && !fileRequests.isEmpty()) {
            for (FileRequest fileRequest : fileRequests) {

                // 최종적으로 post를 저장시에 aws 파일 저장 위치를 temp -> final로 변경하기 때문에 final로 변경하는 로직 추가.
                // 따라서 db에 final 경로로 저장한다.
                String updatedFileUrl = fileRequest.getFileUrl().replace("/temp/", "/final/");
                File file = File.builder()
                        .fileName(fileRequest.getFileName())
                        .filetType(fileRequest.getFileType())
                        .fileUrl(updatedFileUrl)
                        .fileSize(fileRequest.getFileSize())
                        .width(fileRequest.getFileType().startsWith("image/")
                                ? (fileRequest.getWidth() != null ? fileRequest.getWidth() : null)
                                : null)
                        .height(fileRequest.getFileType().startsWith("image/")
                                ? (fileRequest.getHeight() != null ? fileRequest.getHeight() : null)
                                : null)
                        .post(post)
                        .user(user)
                        .build();
                files.add(file);
            }
        }
        return files;
    }


    private List<PostTag> processTags(Post post, List<String> tagNames, User user) {

        List<PostTag> postTags = new ArrayList<>();
        if (tagNames != null && !tagNames.isEmpty()) {
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
        if (featuredImageRequest != null) {

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
