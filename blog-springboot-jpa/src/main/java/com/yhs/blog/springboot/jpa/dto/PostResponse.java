package com.yhs.blog.springboot.jpa.dto;

import com.yhs.blog.springboot.jpa.entity.File;
import com.yhs.blog.springboot.jpa.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostResponse {

    private Long id;             // 게시글 ID

    private Long userId;          // 작성자 ID

    private String userName;        // 작성자명

    private String categoryName;
    // 카테고리 ID
    private String title;         // 게시글 제목

    private String content;       // 게시글 내용

    private List<String> tags;   // 태그

    private List<FileResponse> files;

    private String postStatus;    // 게시글 상태 (PUBLIC, PRIVATE)

    private String commentsEnabled; // 댓글 허용 여부

    private FeaturedImageResponse featuredImage; // 대표 이미지

    private LocalDateTime createdAt; // 생성 일시

    private LocalDateTime updatedAt; // 수정 일시

    private int views;            // 조회수

    private int commentCount;     // 댓글 수

    private int replyCount;       // 대댓글 수


    public PostResponse(Post post) {
        this.id = post.getId();
        this.userId = post.getUser() != null ? post.getUser().getId() : null;
        this.userName = post.getUser() != null ? post.getUser().getUsername() : null;
        this.categoryName = post.getCategory() != null ? post.getCategory().getName() : null;
        this.title = post.getTitle();
        this.content = post.getContent().replace("/temp/", "/final/");
        this.tags = post.getPostTags() != null ?
                post.getPostTags().stream().map(postTag -> postTag.getTag().getName()).collect(Collectors.toList()) :
                null;
        this.files = post.getFiles() != null ?
                post.getFiles().stream().map(FileResponse::new).collect(Collectors.toList()) :
                null;
        this.postStatus = post.getPostStatus().name();
        this.commentsEnabled = post.getCommentsEnabled().name();
        this.featuredImage = post.getFeaturedImage() != null ? new FeaturedImageResponse(post.getFeaturedImage()) : null;
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.views = post.getViews();
        this.commentCount = post.getCommentCount();
        this.replyCount = post.getReplyCount();

    }
}
