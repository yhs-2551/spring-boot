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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final S3Service s3Service;


//    @Transactional
//    @Override
//    public PostResponse createPost(PostRequest postRequest, HttpServletRequest request) {
//
//        try {
//
//            log.info("PostRequest >>>> " + postRequest);
//
////            User user = extractUserFromPrincipal(principal);
//
//            Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
//            User user = userService.findUserById(userId);
//
//            Category category = postRequest.getCategoryId() != null ? categoryRepository.findById(postRequest.getCategoryId()).orElse(null) : null;
//
//            FeaturedImage featuredImage = null;
//
//            if (postRequest.getFeaturedImage() != null) {
//
//
//                // 최종적으로 post를 저장시에 aws 파일 저장 위치를 temp -> final로 변경하기 때문에 final로 변경하는 로직 추가. 따라서
//                // db에 final 경로로 저장한다
//                String updatedFileUrl = postRequest.getFeaturedImage().getFileUrl().replace(
//                        "/temp/", "/final/");
//
//
//                featuredImage = FeaturedImage.builder()
//                        .fileName(postRequest.getFeaturedImage().getFileName())
//                        .fileUrl(updatedFileUrl)
//                        .fileType(postRequest.getFeaturedImage().getFileType())
//                        .fileSize(postRequest.getFeaturedImage().getFileSize())
//                        .build();
//
//                featuredImageRepository.save(featuredImage);
//            }
//
//
//            // 포스트 생성 시 user를 넘겨주면 외래키 연관관계 설정으로 인해 posts테이블에 user_id 값이 자동으로 들어간다.
//            // category, featuredImage또한 마찬가지.
//            Post post = PostMapper.toEntity(user, category, postRequest.getTitle(),
//                    postRequest.getContent(), postRequest.getPostStatus(),
//                    postRequest.getCommentsEnabled(), featuredImage);
//
//
//            if (postRequest.getFiles() != null && !postRequest.getFiles().isEmpty()) {
//                Set<File> files = new HashSet<>();
//                for (FileRequest fileRequest : postRequest.getFiles()) {
//
//                    // 최종적으로 post를 저장시에 aws 파일 저장 위치를 temp -> final로 변경하기 때문에 final로 변경하는 로직 추가.
//                    // 따라서 db에 final 경로로 저장한다.
//                    String updatedFileUrl = fileRequest.getFileUrl().replace("/temp/", "/final/");
//
//                    File file = File.builder()
//                            .fileName(fileRequest.getFileName())
//                            .filetType(fileRequest.getFileType())
//                            .fileUrl(updatedFileUrl)
//                            .fileSize(fileRequest.getFileSize())
//                            .post(post)
//                            .build();
//                    files.add(file);
//
//                }
//
//                log.info("filse >>>>>>>> {}", files);
//
//                post.setFiles(files);
//            }
//
//            if (postRequest.getTags() != null && !postRequest.getTags().isEmpty()) {
//                Set<PostTag> postTags = new HashSet<>();
//                for (String tagName : postRequest.getTags()) {
//                    Tag tag = tagRepository.findByName(tagName).orElseGet(() -> Tag.create(tagName));
//                    PostTag postTag = PostTag.create(post, tag);
//                    postTags.add(postTag);
//                }
//                post.setPostTags(postTags);
//            }
//
//            postRepository.save(post);
//
//            // s3 Temp 파일 관련 작업은 비동기로 처리. 사용자에게 빠르게 응답하기 위함
//            s3Service.processCreatePostS3TempOperation(postRequest);
//
//            return new PostResponse(post);
//
////            throw new DataAccessException("Simulated database exception") {};
//
//        } catch (DataAccessException ex) {
//            throw new RuntimeException("A server error occurred while creating the post.", ex);
//        }
//
//    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPostList() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(PostResponse::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id  " + id));

        return new PostResponse(post);
    }

    @Override
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }


    @Transactional
    @Override
    public PostResponse createPost(PostRequest postRequest, HttpServletRequest request) {
        try {

            log.info("PostRequest >>>> " + postRequest);
            Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
            User user = userService.findUserById(userId);
            Category category = postRequest.getCategoryId() != null ? categoryRepository.findById(postRequest.getCategoryId()).orElse(null) : null;

            FeaturedImage featuredImage = processFeaturedImage(postRequest.getFeaturedImage());

            // 포스트 생성 시 user를 넘겨주면 외래키 연관관계 설정으로 인해 posts테이블에 user_id 값이 자동으로 들어간다.
            // category, featuredImage또한 마찬가지.
            Post post = PostMapper.toEntity(user, category, postRequest.getTitle(), postRequest.getContent(), postRequest.getPostStatus(), postRequest.getCommentsEnabled(), featuredImage);
            post.setFiles(processFiles(post, postRequest.getFiles()));
            post.setPostTags(processTags(post, postRequest.getTags()));

            postRepository.save(post);

            // s3 Temp 파일 관련 작업은 비동기로 처리. 사용자에게 빠르게 응답하기 위함
            s3Service.processCreatePostS3TempOperation(postRequest);

            return new PostResponse(post);

        } catch (DataAccessException ex) {
            throw new RuntimeException("A server error occurred while creating the post.", ex);
        }
    }

//    @Transactional
//    @Override
//    public Post updatePost(Long id, PostUpdateRequest postUpdateRequest) {
//
//        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id  " + id));
//        Category category = postUpdateRequest.getCategoryId() != null ? categoryRepository.findById(postUpdateRequest.getCategoryId()).orElse(null) : null;
//
//        Set<File> newFiles = new HashSet<>();
//
//        if (postUpdateRequest.getFiles() != null && !postUpdateRequest.getFiles().isEmpty()) {
//
//            for (FileRequest fileRequest : postUpdateRequest.getFiles()) {
//
////                수정 시에 에디터에 새롭게 올라갈 파일들 temp -> final로 경로 변경해서 DB에 저장
//                String updatedFileUrl = fileRequest.getFileUrl().replace("/temp/", "/final/");
//
//                File file = File.builder()
//                        .fileName(fileRequest.getFileName())
//                        .filetType(fileRequest.getFileType())
//                        .fileUrl(updatedFileUrl)
//                        .fileSize(fileRequest.getFileSize())
//                        .post(post)
//                        .build();
//                newFiles.add(file);
//
//            }
//        }
//
//        Set<PostTag> newPostTags = new HashSet<>();
//
//        if (postUpdateRequest.getTags() != null && !postUpdateRequest.getTags().isEmpty()) {
//
//            for (String tagName : postUpdateRequest.getTags()) {
//                Tag tag = tagRepository.findByName(tagName).orElseGet(() -> Tag.create(tagName));
//                tagRepository.save(tag);
//                PostTag postTag = PostTag.create(post, tag);
//
//                newPostTags.add(postTag);
//            }
//        }
//
//        FeaturedImage featuredImage = null;
//
//        if (postUpdateRequest.getFeaturedImage() != null) {
//
//            featuredImage = FeaturedImage.builder()
//                    .fileName(postUpdateRequest.getFeaturedImage().getFileName())
//                    .fileUrl(postUpdateRequest.getFeaturedImage().getFileUrl())
//                    .fileType(postUpdateRequest.getFeaturedImage().getFileType())
//                    .fileSize(postUpdateRequest.getFeaturedImage().getFileSize())
//                    .build();
//
//            featuredImageRepository.save(featuredImage);
//        }
//
//        post.update(category, postUpdateRequest.getTitle(), postUpdateRequest.getContent(),
//                newFiles,
//                newPostTags,
//                Post.PostStatus.valueOf(postUpdateRequest.getPostStatus().toUpperCase()),
//                Post.CommentsEnabled.valueOf(postUpdateRequest.getCommentsEnabled().toUpperCase()), featuredImage
//        );
//
//        s3Service.processUpdatePostS3TempOperation(postUpdateRequest);
//
//        return postRepository.save(post);
//    }

    @Transactional
    @Override
    public Post updatePost(Long id, PostUpdateRequest postUpdateRequest) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id " + id));
        Category category = postUpdateRequest.getCategoryId() != null ? categoryRepository.findById(postUpdateRequest.getCategoryId()).orElse(null) : null;

        Set<File> newFiles = processFiles(post, postUpdateRequest.getFiles());
        List<PostTag> newPostTags = processTags(post, postUpdateRequest.getTags());

        if (postUpdateRequest.getEditPageDeletedTags() != null && !postUpdateRequest.getEditPageDeletedTags().isEmpty()) {

            List<Tag> unusedTags =
                    tagRepository.findUnusedTagsNotUsedByOtherPosts(postUpdateRequest.getEditPageDeletedTags(), id);

            tagRepository.deleteAll(unusedTags);
        }


        FeaturedImage featuredImage = processFeaturedImage(postUpdateRequest.getFeaturedImage());

        post.update(category, postUpdateRequest.getTitle(), postUpdateRequest.getContent(), newFiles, newPostTags, Post.PostStatus.valueOf(postUpdateRequest.getPostStatus().toUpperCase()), Post.CommentsEnabled.valueOf(postUpdateRequest.getCommentsEnabled().toUpperCase()), featuredImage);
        s3Service.processUpdatePostS3TempOperation(postUpdateRequest);
        return postRepository.save(post);
    }

    //아래쪽은 헬퍼 메서드

    private Set<File> processFiles(Post post, List<FileRequest> fileRequests) {
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
                        .build();
                files.add(file);
            }
        }
        return files;
    }


    private List<PostTag> processTags(Post post, List<String> tagNames) {

        List<PostTag> postTags = new ArrayList<>();
        if (tagNames != null && !tagNames.isEmpty()) {
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByName(tagName).orElseGet(() -> Tag.create(tagName));
                tagRepository.save(tag);
                PostTag postTag = PostTag.create(post, tag);
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


//    private User extractUserFromPrincipal(Principal principal) {
//        if (principal instanceof UsernamePasswordAuthenticationToken) {
//            //getName에서 리턴하는 값은 USerDetails에서 loadByuserName메서드에서 이메일 값으로 찾아왔기 때문에 email값을 가져오게 된다.
//            String userEmail =  ((UsernamePasswordAuthenticationToken) principal).getName();
//            return userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found with userEmail" + userEmail));
//        } else if (principal instanceof OAuth2AuthenticationToken) {
//            OAuth2User oAuth2User = ((OAuth2AuthenticationToken) principal).getPrincipal();
//            String userEmail = (String) oAuth2User.getAttributes().get("email");
//            return userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found with userEmail" + userEmail));
//        } else {
//            throw new IllegalArgumentException("Unsupported authentication type");
//        }
//    }


}
