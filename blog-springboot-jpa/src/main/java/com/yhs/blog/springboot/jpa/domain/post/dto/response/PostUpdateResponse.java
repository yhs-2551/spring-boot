package com.yhs.blog.springboot.jpa.domain.post.dto.response;


import com.yhs.blog.springboot.jpa.domain.file.dto.response.FileResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostUpdateResponse {

    private String categoryName; // 카테고리명
    private String title;         // 게시글 제목

    private String content;       // 게시글 내용

    private List<String> tags;   // 태그

    private List<FileResponse> files;

    private String postStatus;    // 게시글 상태 (PUBLIC, PRIVATE)

    private String commentsEnabled; // 댓글 허용 여부

    private FeaturedImageResponse featuredImage; // 대표 이미지

    private LocalDateTime updatedAt; // 수정 일시

    private int views;            // 조회수

    private int commentCount;     // 댓글 수

    private int replyCount;       // 대댓글 수


    public PostUpdateResponse(Post post) {
        this.categoryName = post.getCategory() != null ? post.getCategory().getName() : null;
        this.title = post.getTitle();
        this.content = post.getContent();
        this.tags = post.getPostTags() != null ?
                post.getPostTags().stream().map(postTag -> postTag.getTag().getName()).collect(Collectors.toList()) :
                null;
        this.files = post.getFiles() != null ?
                post.getFiles().stream().map(FileResponse::new).collect(Collectors.toList()) :
                null;
        this.postStatus = post.getPostStatus().name();
        this.commentsEnabled = post.getCommentsEnabled().name();
        this.featuredImage = post.getFeaturedImage() != null ? FeaturedImageResponse.from(post.getFeaturedImage()) : null;
        this.updatedAt = post.getUpdatedAt();
        this.views = post.getViews();
        this.commentCount = post.getCommentCount();
        this.replyCount = post.getReplyCount();
    }
}
