package com.yhs.blog.springboot.jpa.dto;

import com.yhs.blog.springboot.jpa.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponse {

    private Long id;             // 게시글 ID

    private Long userId;          // 작성자 ID

    private String userName;        // 작성자명

    private String title;         // 게시글 제목

    private String content;       // 게시글 내용

    private String categoryName;      // 카테고리 ID

    private LocalDateTime createdAt; // 생성 일시

    private LocalDateTime updatedAt; // 수정 일시

    private int views;            // 조회수

    private int commentCount;     // 댓글 수

    private int replyCount;       // 대댓글 수

    private String postStatus;    // 게시글 상태 (PUBLIC, PRIVATE)


    public PostResponse(Post post) {
        this.id = post.getId();
        this.userId = post.getUser() != null ? post.getUser().getId() : null;
        this.userName = post.getUser() != null ? post.getUser().getUsername() : null;
        this.title = post.getTitle();
        this.content = post.getContent();
        this.categoryName = post.getCategory() != null ? post.getCategory().getName() : null;
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.views = post.getViews();
        this.commentCount = post.getCommentCount();
        this.replyCount = post.getReplyCount();
        this.postStatus = post.getPostStatus().name();
    }
}
